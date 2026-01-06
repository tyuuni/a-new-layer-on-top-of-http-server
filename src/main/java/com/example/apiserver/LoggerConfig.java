package com.example.apiserver;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.annotation.Nullable;

import static com.example.apiserver.ConfigConstants.ENABLE_CONSOLE_LOGGING;
import static com.example.apiserver.ConfigConstants.LOGGING_PARENT_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_UNIQUE_ID;
import static com.example.apiserver.ConfigConstants.LOG_ERROR_FILE_PATH;
import static com.example.apiserver.ConfigConstants.LOG_ERROR_FILE_ROLLING;
import static com.example.apiserver.ConfigConstants.LOG_INFO_FILE_PATH;
import static com.example.apiserver.ConfigConstants.LOG_INFO_FILE_ROLLING;
import static com.example.apiserver.ConfigConstants.LOG_LEVEL;

// spring @Order不管用，这么写让logger第一个初始化
public class LoggerConfig {
    static void addAppenderToLogger(final ch.qos.logback.classic.Logger logger,
                                    final LoggerContext context,
                                    @Nullable final Filter<ILoggingEvent> filter,
                                    final String name,
                                    final String path,
                                    final String rollingPath) {

        final var encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(String.format("%%date %%-5level "
                + "[%%method] %%logger{5} \\(%%X{%s},%%X{%s},%%X{%s}\\) - %%msg%%n", LOGGING_UNIQUE_ID, LOGGING_PARENT_TRACING_ID, LOGGING_TRACING_ID));

        encoder.start();

        final var rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setContext(context);
        rollingFileAppender.setName(name);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setFile(path);

        final var rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(rollingPath);
        rollingPolicy.setMaxHistory(30);
        rollingFileAppender.setRollingPolicy(rollingPolicy);

        rollingPolicy.start();
        rollingFileAppender.start();
        if (filter != null) {
            rollingFileAppender.addFilter(filter);
        }
//        StatusPrinter.print(context);
        logger.addAppender(rollingFileAppender);
    }

    @Bean
    public Logger logger(@Value(LOG_LEVEL) final String level,
                         @Value(LOG_INFO_FILE_PATH) final String infoPath,
                         @Value(LOG_INFO_FILE_ROLLING) final String infoRolling,
                         @Value(LOG_ERROR_FILE_PATH) final String errorPath,
                         @Value(LOG_ERROR_FILE_ROLLING) final String errorRolling,
                         @Value(ENABLE_CONSOLE_LOGGING) final boolean enableConsoleLogging) {
        final var context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final var rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        rootLogger.setAdditive(false);
        rootLogger.setLevel(Level.valueOf(level));


        addAppenderToLogger(rootLogger, context, null, "info", infoPath, infoRolling);
        addAppenderToLogger(rootLogger, context, new Filter<>() {
            @Override
            public FilterReply decide(ILoggingEvent event) {
                if (event.getLevel() == Level.ERROR) {
                    return FilterReply.ACCEPT;
                }
                return FilterReply.DENY;
            }
        }, "error", errorPath, errorRolling);

        if (enableConsoleLogging) {
            final var encoder = new PatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern(String.format("%%date %%-5level "
                    + "[%%method] %%logger{5} \\(%%X{%s},%%X{%s},%%X{%s}\\) - %%msg%%n", LOGGING_UNIQUE_ID, LOGGING_PARENT_TRACING_ID, LOGGING_TRACING_ID));

            encoder.start();
            final var consoleAppender = new ConsoleAppender<ILoggingEvent>();
            consoleAppender.setContext(context);
            consoleAppender.setName("console");
            consoleAppender.setEncoder(encoder);
            consoleAppender.start();
            rootLogger.addAppender(consoleAppender);
        }

        return rootLogger;
    }
}
