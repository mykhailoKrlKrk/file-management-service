package com.file.registry.exception;

import org.springframework.http.HttpStatus;

public class FileProcessingException extends RestException {

  public FileProcessingException(String reason, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
  }
}
