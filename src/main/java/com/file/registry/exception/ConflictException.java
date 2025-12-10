package com.file.registry.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends RestException {

    public ConflictException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }
}
