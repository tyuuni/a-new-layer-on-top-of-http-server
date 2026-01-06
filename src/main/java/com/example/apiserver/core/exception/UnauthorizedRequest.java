package com.example.apiserver.core.exception;

import org.apache.http.HttpStatus;

public class UnauthorizedRequest extends ClientSideException {
    public UnauthorizedRequest(final String messageInternal,
                               final String jsonResponse) {
        super(HttpStatus.SC_UNAUTHORIZED, messageInternal, jsonResponse);
    }
}
