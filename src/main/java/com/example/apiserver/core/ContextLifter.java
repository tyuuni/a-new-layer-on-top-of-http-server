package com.example.apiserver.core;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.stream.Collectors;

/**
 * currently not used.
 * this class is for lifting the context from the parent to the child.
 */
class ContextLifter<T> implements CoreSubscriber<T> {
    private final Logger LOGGER = LoggerFactory.getLogger(ContextLifter.class);

    private final CoreSubscriber<T> subscriber;

    ContextLifter(final CoreSubscriber<T> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public Context currentContext() {
        return subscriber.currentContext();
    }

    @Override
    public void onSubscribe(final Subscription s) {
        subscriber.onSubscribe(s);
    }

    @Override
    public void onNext(final T t) {
        copyToMdc();
        subscriber.onNext(t);
    }

    @Override
    public void onError(final Throwable t) {
        subscriber.onError(t);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }

    private void copyToMdc() {
        // TODO: 清空上下文。现在的实现是个粗糙版本，对于executor的上下文，暂时不做清空，只用替换
        if (!subscriber.currentContext().isEmpty()) {
            MDC.setContextMap(subscriber.currentContext().stream()
                    .collect(Collectors.toMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())));
        }
    }
}
