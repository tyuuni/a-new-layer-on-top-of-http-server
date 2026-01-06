package com.example.apiserver;

import brave.Tracing;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.example.apiserver.core.ApiContext;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.service.dao.CourseStudentRefsDAO;
import com.example.apiserver.service.dao.CoursesDAO;
import com.example.apiserver.service.dao.UsersDAO;
import com.example.apiserver.store.TokenStore;
import com.example.apiserver.tracing.RedisSpanCustomizer;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.javalin.Javalin;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.BraveTracing;
import io.opentracing.Tracer;
import io.opentracing.contrib.redis.common.TracingConfiguration;
import io.opentracing.contrib.redis.lettuce.TracingStatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.example.apiserver.ConfigConstants.*;


@Configuration
@Import({LoggerConfig.class, TracingConfig.class})
@PropertySource(value = "file:application.properties", encoding = "UTF-8")
public class AppConfig implements ApplicationContextAware {
    static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    public static ApplicationContext APP_CONTEXT = null;

    public static int SOURCE = 1;

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        APP_CONTEXT = context;
    }

    @Bean
    public ApiContext apiContext(@Autowired final Javalin app,
                                 @Autowired final Tracing tracing) {
        return new ApiContext(app, tracing);
    }

    @Bean
    public Javalin webapp() {
        final var app = Javalin.create(javalinConfig -> {
            javalinConfig.prefer405over404 = true;
        });
        return app;
    }

    @Bean()
    @Lazy // 主要是lettuce初始化太慢，redisClient.connect()要执行五秒，尽量让spring初始化早点结束，并行先注册业务单元
    public TokenStore tokenStore(@Autowired final StatefulRedisConnection<String, String> connection,
                                 @Value("${server.session.ttl}") final int ttl) {
        final var commands = connection.sync();
        return new TokenStore(
                "user:lt:",
                commands,
                ttl
        );
    }

    @Bean
    @Qualifier("async-tasks-redis-connection")
    public StatefulRedisConnection asyncTasksConnection(@Autowired @Qualifier("async-tasks-redis-client") final RedisClient client) {
        return client.connect();
    }

    @Bean
    @Qualifier("async-tasks-redis-client")
    public RedisClient asyncTasksRedisClient(@Value("${redis.url}") final String url,
                                             @Value("${redis.auth}") final String password,
                                             @Value("${redis.db.token}") final int tokenDB,
                                             @Autowired final ClientResources clientResources) {
        final var uriBuilder = RedisURI.builder()
                .withHost(url)
                .withDatabase(tokenDB);
        if (password.length() != 0) {
            uriBuilder.withPassword(password.toCharArray());
        }
        return RedisClient.create(clientResources, uriBuilder.build());
    }

    @Bean
    public ClientResources clientResources(@Autowired final Tracing tracing) {
        final var redisTracing = BraveTracing.builder()
                .excludeCommandArgsFromSpanTags()
                .tracing(tracing)
                .spanCustomizer(new RedisSpanCustomizer())
                .build();
        return ClientResources.builder()
                .tracing(redisTracing)
                .build();
    }

    @Bean
    public StatefulRedisConnection<String, String> statefulRedisConnection(@Autowired RedisClient redisClient,
                                                                           @Autowired Tracer tracer) {
        return new TracingStatefulRedisConnection<>(
                redisClient.connect(),
                new TracingConfiguration.Builder(tracer)
                        .traceWithActiveSpanOnly(true)
                        .build());
    }

    @Bean
    public RedisClient redisClient(@Value("${redis.url}") final String url,
                                   @Value("${redis.auth}") final String auth,
                                   @Value("${redis.db.token}") final int dbToken) {
        final var redisURIBuilder = RedisURI.builder()
                .withHost(url)
                .withTimeout(Duration.of(60, TimeUnit.SECONDS.toChronoUnit()))
                .withDatabase(dbToken);
        if (auth.length() != 0) {
            redisURIBuilder.withPassword(auth.toCharArray());
        }
        final var redisURI = redisURIBuilder.build();
        return RedisClient.create(ClientResources.builder().build(), redisURI);
    }

    @Bean
    public OSSClient ossClient(@Value(ALIYUN_OSS_APP_ID) final String accessKeyId,
                               @Value(ALIYUN_OSS_APP_KEY) final String secretAccessKey,
                               @Value(ALIYUN_OSS_END_POINT) final String endPoint) {
        return new OSSClient(endPoint, new DefaultCredentialProvider(accessKeyId, secretAccessKey), null);
    }

    @Bean
    public TemporaryMonolithicService monolithicService(@Autowired final DataSource master) {
        return new TemporaryMonolithicService(
                master,
                new UsersDAO(),
                new CoursesDAO(),
                new CourseStudentRefsDAO()
        );
    }

    @Bean("mysql-master")
    public DataSource mysqlMaster(@Value(MYSQL_CONFIG_MASTER_URL) final String url,
                                  @Value(MYSQL_CONFIG_USERNAME) final String username,
                                  @Value(MYSQL_CONFIG_PASSWORD) final String password) throws Exception {
        final var cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver"); //loads the jdbc driver
        cpds.setJdbcUrl(url);
        cpds.setUser(username);
        cpds.setPassword(password);
        cpds.setMaxStatements(180);
        cpds.setAutoCommitOnClose(false);
        cpds.setAcquireIncrement(1);
        cpds.setIdleConnectionTestPeriod(3000);
        cpds.setTestConnectionOnCheckin(true);
        cpds.setTestConnectionOnCheckout(true);
        return cpds;
    }
}
