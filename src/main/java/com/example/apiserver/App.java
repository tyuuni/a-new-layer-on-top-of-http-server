package com.example.apiserver;

import ch.qos.logback.classic.LoggerContext;
import com.example.apiserver.apis.AllCourseApis;
import com.example.apiserver.apis.AllUserApis;
import com.example.apiserver.apis.SystemApis;
import com.example.apiserver.core.ApiContext;
import com.example.apiserver.injector.*;
import com.example.apiserver.service.TemporaryMonolithicService;
import com.example.apiserver.store.TokenStore;
import com.example.apiserver.unit.AllUnitsRegister;
import com.example.apiserver.unit.course.SingleCourseByIdGetter;
import com.example.apiserver.unit.user.SingleUserByIdGetter;
import io.javalin.Javalin;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.example.apiserver.ConfigConstants.SERVER_PORT;

// TODO: graceful shutdown
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    static void disableDefaultLogger() {
        final var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final var rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("start initialization");
        disableDefaultLogger();

        final ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        LOGGER.info("spring initialization finished");
        final var apiContext = applicationContext.getBean(ApiContext.class);
        apiContext.initializeFramework();
        apiContext.addGlobalInjector(new RequestMonitor());

        final var tokenStore = applicationContext.getBean(TokenStore.class);
        apiContext.registerUnit(tokenStore);

        final var temporaryMonolithicService = applicationContext.getBean(TemporaryMonolithicService.class);
        AllUnitsRegister.register(apiContext, temporaryMonolithicService);

        final var mpUserAuthenticator = new Authenticator(
                apiContext.getBusinessUnit(SingleUserByIdGetter.class),
                tokenStore);
        apiContext.registerResourceInjector(
                Authenticator.LoggedInUser.class,
                paths -> mpUserAuthenticator
        );

        final StatefulRedisConnection<String, String> redisConnection = applicationContext.getBean(StatefulRedisConnection.class);

        final var apiBasedSemaphore = new ApiBasedSemaphore(redisConnection);
        apiContext.registerResourceInjector(
                ApiBasedSemaphore.InjectedApiSemaphore.class,
                paths -> apiBasedSemaphore
        );

        final var apiCacheLayer = new ApiCacheLayer(redisConnection);
        apiContext.registerResourceInjector(
                ApiCacheLayer.CacheStatus.class,
                paths -> apiCacheLayer
        );

        apiContext.registerResourceInjector(
                SingleUserInjector.InjectedUser.class,
                paths -> new SingleUserInjector(apiContext.getBusinessUnit(SingleUserByIdGetter.class), paths[0])
        );

        apiContext.registerResourceInjector(
                SingleCourseInjector.InjectedCourse.class,
                paths -> new SingleCourseInjector(apiContext.getBusinessUnit(SingleCourseByIdGetter.class), paths[0])
        );

        SystemApis.initialize(apiContext);
        AllUserApis.initializeAll(apiContext);
        AllCourseApis.initializeAll(apiContext);

        final var app = applicationContext.getBean(Javalin.class);
        app.start(applicationContext.getEnvironment().getProperty(SERVER_PORT, Integer.class));
    }
}
