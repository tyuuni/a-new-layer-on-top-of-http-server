package com.example.apiserver.core.exception;

import org.apache.http.HttpStatus;

public class UnauthenticatedRequest extends ClientSideException {
    public UnauthenticatedRequest(final String messageInternal,
                                  final String jsonResponse) {
        super(HttpStatus.SC_UNAUTHORIZED, messageInternal, jsonResponse);
    }
}
