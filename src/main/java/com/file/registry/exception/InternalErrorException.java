package com.file.registry.exception;

import org.springframework.http.HttpStatus;

public class InternalErrorException extends RestException {

  public InternalErrorException(String reason, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
  }
}
