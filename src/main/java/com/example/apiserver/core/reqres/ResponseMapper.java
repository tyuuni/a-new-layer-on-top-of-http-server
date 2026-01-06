package com.example.apiserver.core.reqres;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;

import javax.annotation.Nullable;

public interface ResponseMapper<T> {
    void mapResponse(Context context, T result);

    HttpCode getStatusCode();

    /**
     * nullable for empty body response, for example 204.
     */
    @Nullable
    ContentType getContentType();

    Class<?> getModel();
}
