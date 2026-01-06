package com.example.apiserver.core.exception;

import org.apache.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final int status;
    private final String jsonResponse;

    public ApiException(final int status,
                        final String messageInternal,
                        final String jsonResponse) {
        super(messageInternal);
        this.status = status;
        this.jsonResponse = jsonResponse;
    }

    public int getStatus() {
        return status;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }
}
