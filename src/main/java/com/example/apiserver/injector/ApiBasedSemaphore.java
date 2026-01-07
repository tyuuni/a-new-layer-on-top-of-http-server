package com.example.apiserver.injector;

import com.example.apiserver.ErrorCodes;
import com.example.apiserver.core.HttpParameter;
import com.example.apiserver.core.InjectedResource;
import com.example.apiserver.core.ResourceInjector;
import com.example.apiserver.core.exception.CodeMessageErrorResponse;
import com.example.apiserver.util.JSONUtil;
import com.google.common.collect.ImmutableList;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.lettuce.core.api.StatefulRedisConnection;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ApiBasedSemaphore implements ResourceInjector<ApiBasedSemaphore.InjectedApiSemaphore> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);

    private final StatefulRedisConnection<String, String> connection;

    public ApiBasedSemaphore(final StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
    }

    private static String key(final String method, final String url) {
        return String.format("smp:%s/%s", method, url);
    }

    private static final String INJECTION_KEY = "apiBasedSemaphore";

    @Override
    public boolean inject(final Context context) {
        final long acquiredValue = connection.sync().incr(key(context.method(), context.path()));
        context.attribute(INJECTION_KEY, InjectedApiSemaphore.of(acquiredValue));
        return true;
    }

    @Override
    public InjectedApiSemaphore extract(final Context context) {
        return context.attribute(INJECTION_KEY);
    }

    @Override
    public void postHandle(final Context context) {
        connection.sync().decr(key(context.method(), context.path()));
    }

    private static final List<HttpParameter> PARAMETERS = ImmutableList.of(
            HttpParameter.pathVariable("raw url"));
    @Override
    public List<HttpParameter> getParameters() {
        return PARAMETERS;
    }

    @Override
    public HttpCode getFailureCode() {
        return HttpCode.CONFLICT;
    }

    @Override
    public List<String> getFailureResponses() {
        return Collections.emptyList();
    }

    @Override
    public Class<InjectedApiSemaphore> getResourceClass() {
        return InjectedApiSemaphore.class;
    }


    @Value.Immutable
    public static abstract class InjectedApiSemaphore implements InjectedResource {
        public abstract long getAcquiredValue();

        static InjectedApiSemaphore of(final long acquiredValue) {
            return ImmutableInjectedApiSemaphore.builder()
                    .acquiredValue(acquiredValue)
                    .build();
        }
    }
}
