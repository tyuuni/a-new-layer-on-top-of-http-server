package com.example.apiserver.tracing.concurrent;


import brave.Span;
import brave.Tracing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Pavol Loffay
 *
 * Executor which propagates span from parent thread to submitted.
 * Optionally it creates parent span if traceWithActiveSpanOnly = false.
 */
public class TracedExecutorService extends TracedExecutor implements ExecutorService {

    private final ExecutorService delegate;

    public TracedExecutorService(final ExecutorService delegate,
                                 final Tracing tracing) {
        super(delegate, tracing);
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return delegate.awaitTermination(l, timeUnit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        Span span = createSpan();
        try {
            return delegate.submit(new TracedCallable<T>(callable, tracing, span));
        } finally {
            span.finish();
        }
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        Span span = createSpan();
        try {
            return delegate.submit(new TracedRunnable(runnable, tracing, span), t);
        } finally {
            span.finish();
        }
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        Span span = createSpan();
        try {
            return delegate.submit(new TracedRunnable(runnable, tracing, span));
        } finally {
            span.finish();
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection)
            throws InterruptedException {
        Span span = createSpan();
        try {
            return delegate.invokeAll(toTraced(collection, span));
        } finally {
            span.finish();
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l,
                                         TimeUnit timeUnit) throws InterruptedException {
        Span span = createSpan();
        try {
            return delegate.invokeAll(toTraced(collection, span), l, timeUnit);
        } finally {
            span.finish();
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection)
            throws InterruptedException, ExecutionException {
        Span span = createSpan();
        try {
            return delegate.invokeAny(toTraced(collection, span));
        } finally {
            span.finish();
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        final var span = createSpan();
        try {
            return delegate.invokeAny(toTraced(collection, span), l, timeUnit);
        } finally {
            span.finish();
        }
    }

    private <T> Collection<? extends Callable<T>> toTraced(final Collection<? extends Callable<T>> delegate,
                                                           final Span toActivate) {
        List<Callable<T>> tracedCallables = new ArrayList<>(delegate.size());

        for (Callable<T> callable: delegate) {
            tracedCallables.add(new TracedCallable<T>(callable, tracing, toActivate));
        }

        return tracedCallables;
    }
}