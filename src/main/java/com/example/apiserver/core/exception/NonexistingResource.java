package com.example.apiserver.core.exception;

import org.apache.http.HttpStatus;

public class NonexistingResource extends ClientSideException {
    public NonexistingResource(final String messageInternal,
                               final String jsonResponse) {
        super(HttpStatus.SC_NOT_FOUND, messageInternal, jsonResponse);
    }
}
