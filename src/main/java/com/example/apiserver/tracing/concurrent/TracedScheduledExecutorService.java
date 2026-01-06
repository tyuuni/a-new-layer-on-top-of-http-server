package com.example.apiserver.tracing.concurrent;

import brave.Tracing;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Jose Montoya
 *
 * Executor which propagates span from parent thread to scheduled.
 * Optionally it creates parent span if traceWithActiveSpanOnly = false.
 */
public class TracedScheduledExecutorService extends TracedExecutorService implements ScheduledExecutorService {

    private final ScheduledExecutorService delegate;

    public TracedScheduledExecutorService(final ScheduledExecutorService delegate,
                                          final Tracing tracing) {
        super(delegate, tracing);
        this.delegate = delegate;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        final var span = createSpan();
        try {
            return delegate.schedule(new TracedRunnable(runnable, tracing, span), delay, timeUnit);
        } finally {
            span.finish();
        }
    }

    @Override
    public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit timeUnit) {
        final var span = createSpan();
        try {
            return delegate.schedule(new TracedCallable<T>(callable, tracing, span), delay, timeUnit);
        } finally {
            span.finish();
        }
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period,
                                                  TimeUnit timeUnit) {
        final var span = createSpan();
        try {
            return delegate.scheduleAtFixedRate(new TracedRunnable(runnable, tracing, span), initialDelay, period, timeUnit);
        } finally {
            span.finish();
        }
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay,
                                                     TimeUnit timeUnit) {
        final var span = createSpan();
        try {
            return delegate.scheduleWithFixedDelay(new TracedRunnable(runnable, tracing, span), initialDelay, delay, timeUnit);
        } finally {
            span.finish();
        }
    }
}