package com.example.apiserver.core.exception;

import org.apache.http.HttpStatus;

public class RequestValidationFailure extends ClientSideException {
    public RequestValidationFailure(final String messageInternal,
                                    final String jsonResponse) {
        super(HttpStatus.SC_NOT_ACCEPTABLE, messageInternal, jsonResponse);
    }
}
