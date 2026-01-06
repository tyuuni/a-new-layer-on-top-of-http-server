package com.example.apiserver.tracing.concurrent;


import brave.Span;
import brave.Tracing;
import org.slf4j.MDC;

import static com.example.apiserver.ConfigConstants.LOGGING_PARENT_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_TRACING_ID;
import static com.example.apiserver.ConfigConstants.LOGGING_UNIQUE_ID;

/**
 * @author Pavol Loffay
 */
public class TracedRunnable implements Runnable {

    private final Runnable delegate;
    private final Span span;
    private final Tracing tracing;

    public TracedRunnable(final Runnable delegate,
                          final Tracing tracing,
                          final Span span) {
        this.delegate = delegate;
        this.tracing = tracing;
        this.span = span;
    }

    @Override
    public void run() {
        try {
            tracing.tracer().withSpanInScope(span);
            MDC.put(LOGGING_TRACING_ID, span.context().traceIdString());
            MDC.put(LOGGING_UNIQUE_ID, span.context().spanIdString());
            MDC.put(LOGGING_PARENT_TRACING_ID, span.context().parentIdString());
            delegate.run();
        } finally {
            MDC.clear();
        }
    }
}