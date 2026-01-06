package com.example.apiserver.core.exception;

import org.apache.http.HttpStatus;

public class ClientSideException extends ApiException {
    public ClientSideException(final int status,
                               final String messageInternal,
                               final String jsonResponse) {
        super(status, messageInternal, jsonResponse);
    }
}
