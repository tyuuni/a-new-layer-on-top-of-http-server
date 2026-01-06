package com.example.apiserver.core.reqres;

import com.example.apiserver.core.ExampleResponse;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

import javax.annotation.Nullable;
import java.util.List;

public interface RequestValidator<T> {
    T validate(Context context);

    Class<T> getModel();

    @Nullable
    ContentType getContentType();

    List<ExampleResponse> getFailureResponses();
}
