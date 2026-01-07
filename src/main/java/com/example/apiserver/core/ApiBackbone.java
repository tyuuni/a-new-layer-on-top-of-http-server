package com.example.apiserver.core;

import brave.ScopedSpan;
import brave.Tracing;
import com.example.apiserver.core.exception.ClientSideException;
import com.example.apiserver.core.exception.CodeMessageErrorResponse;
import com.example.apiserver.core.reqres.RequestValidator;
import com.example.apiserver.core.reqres.ResponseMapper;
import com.google.common.collect.Lists;
import com.example.apiserver.ErrorCodes;
import com.example.apiserver.tracing.concurrent.TracedScheduledExecutorService;
import com.example.apiserver.util.JSONUtil;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;

import static com.example.apiserver.ConfigConstants.LOGGING_PARENT_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_UNIQUE_ID;


class ApiBackbone {
    private final static Logger LOGGER = LoggerFactory.getLogger(ApiBackbone.class);

    private static final String UNEXPECTED_EXCEPTION = JSONUtil.writeAsJson(
            CodeMessageErrorResponse.of(
                    ErrorCodes.FATAL.value(),
                    "logic error")
    );

    static Tracing tracing;

    private static BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService> decoratorWithFallback() {
        return (scheduler, scheduledExecutorService) -> {
            return new TracedScheduledExecutorService(scheduledExecutorService, tracing);
        };
    }

    static {
        // we have to write in this way to correctly initialize reactor correctly.
        // see https://www.geeksforgeeks.org/order-execution-initialization-blocks-constructors-java/
        Schedulers.setExecutorServiceDecorator(ApiBackbone.class.getName(), decoratorWithFallback());
    }

    private final Javalin app;
    private final List<ResourceInjector<?>> globalInjectors;
    private final List<ApiDefinition<?, ?>> apis;

    ApiBackbone(final Javalin app,
                final Tracing tracing) {
        this.app = app;
        ApiBackbone.tracing = tracing;
        this.apis = new ArrayList<>();
        this.globalInjectors = new ArrayList<>();
    }

    List<ApiDefinition<?, ?>> getApis() {
        return apis;
    }

    /**
     * processing order:
     * 1. global injectors
     * 2. resource injectors
     * 3. validator
     * 5. handler
     *
     * @param injectors
     * @return
     */
    <R, T> ApiDefinition<R, T> addApi(final HandlerType type,
                                      final String path,
                                      final String description,
                                      final List<ResourceInjector<?>> injectors,
                                      final List<? extends BusinessUnit> units,
                                      final RequestValidator<R> validator,
                                      final GeneralizedHandler<Mono<T>> handler,
                                      final ResponseMapper<T> responseMapper) {
        final var copiedInjectors = Lists.newArrayList(injectors);
        final var copiedUnits = Lists.<BusinessUnit>newArrayList(units);
        final var paramsSize = copiedInjectors.size() + copiedUnits.size() + 1;
        app.addHandler(
                type,
                path,
                wrap(context -> {
                    // TODO: remove duplicated codes
                    int lastCalledInjector = -1;
                    try {
                        final var params = new Object[paramsSize];
                        boolean shouldSkipHandler = false;
                        for (int i = 0; i < copiedInjectors.size(); i++) {
                            lastCalledInjector = i;
                            final var isInjected = copiedInjectors.get(i).inject(context);
                            if (!isInjected) {
                                shouldSkipHandler = true;
                                break;
                            }
                            params[i] = copiedInjectors.get(i).extract(context);
                        }
                        if (!shouldSkipHandler) {
                            final var request = validator.validate(context);
                            for (int i = 0; i < copiedUnits.size(); i++) {
                                params[i + copiedInjectors.size()] = copiedUnits.get(i);
                            }
                            params[paramsSize - 1] = request;
                            final var span = ((ScopedSpan) context.attribute(INJECTED_SPAN));
                            final var result = handler.apply(params)
                                    .subscriberContext(
                                            Context.of(
                                                    LOGGING_TRACING_ID, span.context().traceIdString(),
                                                    LOGGING_UNIQUE_ID, span.context().spanIdString(),
                                                    LOGGING_PARENT_TRACING_ID, ""))
                                    .block();
                            responseMapper.mapResponse(context, result);
                        }
                    } catch (final ClientSideException e) {
                        context
                                .status(e.getStatus())
                                .contentType(ContentType.APPLICATION_JSON)
                                .result(e.getJsonResponse());
                    } catch (final Exception e) {
                        LOGGER.error("fatal error", e);
                        context
                                .status(HttpCode.INTERNAL_SERVER_ERROR)
                                .contentType(ContentType.APPLICATION_JSON)
                                .result(UNEXPECTED_EXCEPTION);
                    }
                    try {
                        for (; lastCalledInjector >= 0; lastCalledInjector--) {
                            copiedInjectors.get(lastCalledInjector).postHandle(context);
                        }
                    } catch (final Exception e) {
                        LOGGER.error(
                                String.format("%s injector postHandle fatal error", copiedInjectors.get(lastCalledInjector).getResourceClass()),
                                e);
                    }
                }));
        final var apiDefinition = ImmutableApiDefinition.<R, T>builder()
                .type(type)
                .path(path)
                .description(description)
                .injectors(copiedInjectors)
                .units(copiedUnits)
                .validator(validator)
                .responseMapper(responseMapper)
                .build();
        apis.add(apiDefinition);
        return apiDefinition;
    }

    private Handler wrap(final Handler handler) {
        return ctx -> {
            int lastCalledInjector = -1;
            try {
                boolean shouldSkipHandler = false;
                for (int i = 0; i < globalInjectors.size(); i++) {
                    lastCalledInjector = i;
                    if (!globalInjectors.get(i).inject(ctx)) {
                        shouldSkipHandler = true;
                        break;
                    }
                }
                if (!shouldSkipHandler) {
                    handler.handle(ctx);
                }
            } catch (final ClientSideException e) {
                ctx
                        .status(e.getStatus())
                        .contentType(ContentType.APPLICATION_JSON)
                        .result(e.getJsonResponse());
            } catch (final Exception e) {
                LOGGER.error("fatal error", e);
                ctx
                        .status(HttpCode.INTERNAL_SERVER_ERROR)
                        .contentType(ContentType.APPLICATION_JSON)
                        .result(UNEXPECTED_EXCEPTION);
            }
            try {
                for (; lastCalledInjector >= 0; lastCalledInjector--) {
                    globalInjectors.get(lastCalledInjector).postHandle(ctx);
                }
            } catch (final Exception e) {
                LOGGER.error(
                        String.format("%s injector postHandle fatal error", globalInjectors.get(lastCalledInjector).getResourceClass()),
                        e);
            }
        };
    }

    void addGlobalInjector(final ResourceInjector<?> injector) {
        globalInjectors.add(injector);
    }

    private static final String INJECTED_SPAN = "span";

    void initializeFramework() {
        app.before(context -> {
            final var span = tracing.tracer().startScopedSpan("http");
            MDC.put(LOGGING_TRACING_ID, span.context().traceIdString());
            MDC.put(LOGGING_UNIQUE_ID, span.context().spanIdString());
            MDC.put(LOGGING_PARENT_TRACING_ID, "");
            context.attribute(INJECTED_SPAN, span);
        });
        app.after(context -> {
            ((ScopedSpan) context.attribute(INJECTED_SPAN)).finish();
            MDC.clear();
        });
    }
}
