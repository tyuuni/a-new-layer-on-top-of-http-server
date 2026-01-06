package com.example.apiserver.core;

import org.immutables.value.Value;

@Value.Immutable
public abstract class HttpParameter {
    enum Type {
        HEADER,
        PATH_VARIABLE
    }

    public abstract Type getType();

    public abstract String getName();

    public static HttpParameter of(final Type type,
                                   final String name) {
        return ImmutableHttpParameter.builder()
                .type(type)
                .name(name)
                .build();
    }

    public static HttpParameter header(final String name) {
        return ImmutableHttpParameter.builder()
                .type(Type.HEADER)
                .name(name)
                .build();
    }

    public static HttpParameter pathVariable(final String name) {
        return ImmutableHttpParameter.builder()
                .type(Type.PATH_VARIABLE)
                .name(name)
                .build();
    }
}
