package com.example.apiserver.tracing;

import brave.Span;
import brave.Tracing;
import brave.propagation.ThreadLocalSpan;
import com.example.apiserver.AppConfig;
import com.example.apiserver.TracingConfig;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptorV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.net.URI;
import java.sql.SQLException;
import java.util.Properties;

import static brave.Span.Kind.CLIENT;
import static com.example.apiserver.ConfigConstants.BRAVE_PROPAGATION_DEBUG_FIELD;
import static com.example.apiserver.ConfigConstants.LOGGING_PARENT_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_UNIQUE_ID;

/**
 * This is actually part of {@link TracingConfig}.
 * It's written in this way because {@link javax.sql.DataSource} does not offer a way to explicitly set {@link StatementInterceptorV2}.
 * Check https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html for more info.
 */
public class MysqlZipkinInterceptor implements StatementInterceptorV2 {
    private static final Logger LOG = LoggerFactory.getLogger(MysqlZipkinInterceptor.class);

    private Tracing tracing = null;

    static void parseServerIpAndPort(Connection connection, Span span) {
        try {
            final URI url = URI.create(connection.getMetaData().getURL().substring(5)); // strip "jdbc:"
            String remoteServiceName = connection.getProperties().getProperty("zipkinServiceName");
            if (remoteServiceName == null || "".equals(remoteServiceName)) {
                String databaseName = connection.getCatalog();
                if (databaseName != null && !databaseName.isEmpty()) {
                    remoteServiceName = "mysql-" + databaseName;
                } else {
                    remoteServiceName = "mysql";
                }
            }
            span.remoteServiceName(remoteServiceName);
            String host = connection.getHost();
            if (host != null) {
                span.remoteIpAndPort(host, url.getPort() == -1 ? 3306 : url.getPort());
            }
        } catch (Exception e) {
        }
    }

    private Tracing getTracing() {
        if (tracing == null) {
            tracing = AppConfig.APP_CONTEXT.getBean(Tracing.class);
        }
        return tracing;
    }

    @Override
    public void init(Connection conn, Properties props) throws SQLException {
    }

    @Override
    public ResultSetInternalMethods preProcess(@Nullable final String sql,
                                               final Statement interceptedStatement,
                                               final Connection connection) throws SQLException {
        final var tracing = getTracing();
        final var context = tracing.currentTraceContext().get();
        final var sqlStr = sql != null ? sql : interceptedStatement.toString();
        LOG.info("sql exec: {}", sqlStr);
        if (context == null) {
            return null;
        }
        final var span = ThreadLocalSpan.CURRENT_TRACER.next();
        final var enableDebug = "true".equals(BRAVE_PROPAGATION_DEBUG_FIELD.getValue(span.context()));
        if (enableDebug) {
//            span.customizer().tag("sql", sqlStr);
        }
        final int spaceIndex = sqlStr.indexOf(' '); // Allow span names of single-word statements like COMMIT
        span.kind(CLIENT).name(spaceIndex == -1 ? sqlStr : sqlStr.substring(0, spaceIndex));
        parseServerIpAndPort(connection, span);
        span.start();
        MDC.put(LOGGING_TRACING_ID, span.context().traceIdString());
        MDC.put(LOGGING_UNIQUE_ID, span.context().spanIdString());
        MDC.put(LOGGING_PARENT_TRACING_ID, span.context().parentIdString());
        return null;
    }

    @Override
    public boolean executeTopLevelOnly() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public ResultSetInternalMethods postProcess(final String sql,
                                                final Statement interceptedStatement,
                                                final ResultSetInternalMethods originalResultSet,
                                                final Connection connection,
                                                final int warningCount,
                                                final boolean noIndexUsed,
                                                final boolean noGoodIndexUsed,
                                                final SQLException statementException) throws SQLException {
        final var tracing = getTracing();
        final var span = ThreadLocalSpan.CURRENT_TRACER.remove();
        if (statementException != null) {
            LOG.error("sql exec failed", statementException);
        } else {
            LOG.info("sql completed");
        }
        if (span == null || span.isNoop()) {
            return null;
        }
        try {
            span.customizer().tag("sql-row-cnt", String.valueOf(originalResultSet.getRow()));
        } catch (final Exception e) {

        }
        if (statementException != null) {
            span.error(statementException);
            span.tag("error", Integer.toString(statementException.getErrorCode()));
        }
        span.finish();
        final var context = tracing.currentTraceContext().get();
        MDC.put(LOGGING_TRACING_ID, context.traceIdString());
        MDC.put(LOGGING_UNIQUE_ID, context.spanIdString());
        MDC.put(LOGGING_PARENT_TRACING_ID, context.parentIdString());
        return null;
    }
}