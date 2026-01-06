package com.example.apiserver.tracing.concurrent;

import brave.Span;
import brave.Tracing;
import java.util.concurrent.Executor;

public class TracedExecutor implements Executor {

    protected final Tracing tracing;
    private final Executor delegate;

    public TracedExecutor(Executor executor, Tracing tracing) {
        this.delegate = executor;
        this.tracing = tracing;
    }

    @Override
    public void execute(final Runnable runnable) {
        final var span = tracing.tracer().nextSpan();
        try {
            delegate.execute(new TracedRunnable(runnable, tracing, span));
        } finally {
            span.finish();
        }
    }

    Span createSpan() {
        return tracing.tracer().nextSpan();
    }
}
