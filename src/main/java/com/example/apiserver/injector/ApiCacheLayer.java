package com.example.apiserver.injector;

import com.example.apiserver.core.ExampleResponse;
import com.example.apiserver.core.HttpParameter;
import com.example.apiserver.core.InjectedResource;
import com.example.apiserver.core.ResourceInjector;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.Collections;
import java.util.List;

public class ApiCacheLayer implements ResourceInjector<ApiCacheLayer.CacheStatus> {
    private final StatefulRedisConnection<String, String> connection;

    public ApiCacheLayer(final StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
    }

    private static String key(final String url) {
        return String.format("rc:%s", url);
    }

    private static final String CACHE_OPERATION_HEADER = "apiCacheStatus";

    @Override
    public boolean inject(final Context context) {
        context.attribute(CACHE_OPERATION_HEADER, new CacheStatus());
        if (context.method().equals("GET")) {
            final String cachedResponse = connection.sync().get(key(context.path()));
            if (cachedResponse != null) {
                context.status(HttpCode.OK)
                        .header("api-cache-hit", "true") // just for demo
                        .contentType(ContentType.APPLICATION_JSON)
                        .result(cachedResponse);
                return false;
            }
        }
        return true;
    }

    @Override
    public CacheStatus extract(final Context context) {
        return context.attribute(CACHE_OPERATION_HEADER);
    }

    @Override
    public void postHandle(final Context context) {
        final CacheStatus cacheStatus = context.attribute(CACHE_OPERATION_HEADER);
        if (cacheStatus.shouldResetCache) {
            if (cacheStatus.responseToCache != null) {
                connection.sync().set(key(context.path()), cacheStatus.responseToCache);
            } else {
                connection.sync().del(key(context.path()));
            }
        }
    }

    @Override
    public List<HttpParameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public List<ExampleResponse> getFailureResponses() {
        return Collections.emptyList();
    }

    @Override
    public Class<CacheStatus> getResourceClass() {
        return CacheStatus.class;
    }

    public static class CacheStatus implements InjectedResource {
        private CacheStatus() {}

        public boolean shouldResetCache = false;
        public String responseToCache = null;
    }
}
