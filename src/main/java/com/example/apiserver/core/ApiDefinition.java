package com.example.apiserver.core;

import com.example.apiserver.core.reqres.RequestValidator;
import com.example.apiserver.core.reqres.ResponseMapper;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpCode;
import org.immutables.value.Value;

import java.util.ArrayList;

import java.util.List;

// TODO: better implementation
@Value.Immutable
public abstract class ApiDefinition<R, T> {
    private final List<ExampleResponse> customResponseExamples = new ArrayList<>();

    public abstract HandlerType getType();

    public abstract String getPath();

    public abstract String getDescription();

    public abstract List<ResourceInjector<?>> getInjectors();

    public abstract List<BusinessUnit> getUnits();

    public abstract RequestValidator<R> getValidator();

    public abstract ResponseMapper<T> getResponseMapper();

    public ApiDefinition<R, T> examplesFor403(final String... examples) {
        for (final var example : examples) {
            customResponseExamples.add(ExampleResponse.of(HttpCode.UNAUTHORIZED, example));
        }
        return this;
    }

    public ApiDefinition<R, T> examplesFor406(final String... examples) {
        for (final var example : examples) {
            customResponseExamples.add(ExampleResponse.of(HttpCode.NOT_ACCEPTABLE, example));
        }
        return this;
    }

    public List<ExampleResponse> getCustomResponseExamples() {
        return customResponseExamples;
    }
}
