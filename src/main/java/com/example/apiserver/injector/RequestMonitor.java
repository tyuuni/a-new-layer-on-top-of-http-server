package com.example.apiserver.injector;

import com.example.apiserver.core.HttpParameter;
import com.example.apiserver.core.InjectedResource;
import com.example.apiserver.core.ResourceInjector;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class RequestMonitor implements ResourceInjector<RequestMonitor.MonitorStatus> {

    private static Logger LOGGER = LoggerFactory.getLogger(RequestMonitor.class);

    private static final RequestMonitor.MonitorStatus DUMMY_TRACING_OBJECT = new RequestMonitor.MonitorStatus();

    @Override
    public RequestMonitor.MonitorStatus extract(final Context context) {
        return DUMMY_TRACING_OBJECT;
    }
    private static final String INJECTED_REQUEST_START_TIME = "reqStart";

    @Override
    public boolean inject(final Context context) {
        LOGGER.info("started processing {}:{}. content-type: {}, content-length: {}",
                context.method(), context.path(), context.contentType(), context.contentLength());
        final var startTime = Instant.now();
        context.attribute(INJECTED_REQUEST_START_TIME, startTime.toEpochMilli());
        return true;
    }

    @Override
    public void postHandle(final Context context) {
        final var endTime = Instant.now();
        LOGGER.info("finished processing. {} {}:{} in {}ms", context.status(), context.method(), context.path(), endTime.toEpochMilli() - (long) context.attribute(INJECTED_REQUEST_START_TIME));
    }

    @Override
    public List<HttpParameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public HttpCode getFailureCode() {
        return HttpCode.INTERNAL_SERVER_ERROR;
    }

    @Override
    public List<String> getFailureResponses() {
        return Collections.emptyList();
    }

    @Override
    public Class<RequestMonitor.MonitorStatus> getResourceClass() {
        return RequestMonitor.MonitorStatus.class;
    }

    public static class MonitorStatus implements InjectedResource {
        MonitorStatus() {}
    }
}
