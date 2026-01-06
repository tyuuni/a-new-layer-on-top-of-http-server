package com.example.apiserver.core.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCodeMessageErrorResponse.class)
public abstract class CodeMessageErrorResponse {
    @Value.Parameter
    public abstract int getCode();

    @Value.Parameter
    public abstract String getMessage();

    public static CodeMessageErrorResponse of(final int code, final String message) {
        return ImmutableCodeMessageErrorResponse.builder()
            .code(code)
            .message(message)
            .build();
    }
}
