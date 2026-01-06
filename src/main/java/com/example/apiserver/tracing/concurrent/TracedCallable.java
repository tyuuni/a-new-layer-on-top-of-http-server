package com.example.apiserver.tracing.concurrent;

import brave.Span;
import brave.Tracing;
import org.slf4j.MDC;

import java.util.concurrent.Callable;

import static com.example.apiserver.ConfigConstants.LOGGING_PARENT_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_UNIQUE_ID;


public class TracedCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final Span span;
    private final Tracing tracing;

    public TracedCallable(final Callable<V> delegate,
                          final Tracing tracing,
                          final Span span) {
        this.delegate = delegate;
        this.tracing = tracing;
        this.span = span;
    }

    @Override
    public V call() throws Exception {
        try {
            tracing.tracer().withSpanInScope(span);
            MDC.put(LOGGING_TRACING_ID, span.context().traceIdString());
            MDC.put(LOGGING_UNIQUE_ID, span.context().spanIdString());
            MDC.put(LOGGING_PARENT_TRACING_ID, span.context().parentIdString());
            return delegate.call();
        } finally {
            MDC.clear();
        }
    }
}
