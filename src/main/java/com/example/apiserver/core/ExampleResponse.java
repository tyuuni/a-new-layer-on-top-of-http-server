package com.example.apiserver.core;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.javalin.http.HttpCode;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableExampleResponse.class)
public abstract class ExampleResponse {
    public abstract int getStatusCode();

    public abstract String getExample();

    public static ExampleResponse of(final HttpCode code,
                                     final String example) {
        return ImmutableExampleResponse.builder()
                .statusCode(code.getStatus())
                .example(example)
                .build();
    }
}
