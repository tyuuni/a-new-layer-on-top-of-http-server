package com.example.apiserver.core.exception;

public class ConflictingRequest extends ApiException {
    public ConflictingRequest(final String messageInternal,
                              final String jsonResponse) {
        super(409, messageInternal, jsonResponse);
    }
}
