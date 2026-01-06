package com.example.apiserver.core.reqres;

import com.example.apiserver.apis.model.EntityCreationResponse;
import com.example.apiserver.util.JSONUtil;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ResponseMapperFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseMapperFactory.class);

    public static <T> ResponseMapper<T> jsonResponseMapper200(final Class<T> model) {
        if (List.class.isAssignableFrom(model)) {
            LOGGER.error("json response should not be of type array for better adaptability");
            throw new IllegalArgumentException("json response should not be of type array");
        }
        return new ResponseMapper<>() {
            @Override
            public void mapResponse(final Context context,
                                    final T result) {
                context.status(HttpCode.OK)
                        .contentType(ContentType.APPLICATION_JSON)
                        .result(JSONUtil.writeAsJson(result));
            }

            @Override
            public HttpCode getStatusCode() {
                return HttpCode.OK;
            }

            @Override
            public Class<T> getModel() {
                return model;
            }

            @Override
            public ContentType getContentType() {
                return ContentType.APPLICATION_JSON;
            }
        };
    }

    public static ResponseMapper<String> singleEntityIdMapper201() {
        return new ResponseMapper<>() {
            @Override
            public void mapResponse(final Context context,
                                    final String id) {
                context.status(HttpCode.CREATED)
                        .contentType(ContentType.APPLICATION_JSON)
                        .result(String.format("{\"id\": \"%s\"}", id));
            }

            @Override
            public HttpCode getStatusCode() {
                return HttpCode.CREATED;
            }

            @Override
            public Class<EntityCreationResponse> getModel() {
                return EntityCreationResponse.class;
            }

            @Override
            public ContentType getContentType() {
                return ContentType.APPLICATION_JSON;
            }
        };
    }

    public static ResponseMapper<Void> noContentMapper204() {
        return new ResponseMapper<>() {
            @Override
            public void mapResponse(final Context context,
                                    final Void any) {
                context.status(HttpCode.NO_CONTENT);
            }

            @Override
            public HttpCode getStatusCode() {
                return HttpCode.NO_CONTENT;
            }

            @Override
            public Class<Void> getModel() {
                return Void.class;
            }

            @Override
            public ContentType getContentType() {
                return null;
            }
        };
    }
}
