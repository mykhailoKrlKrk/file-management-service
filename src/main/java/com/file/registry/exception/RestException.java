package com.file.registry.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class RestException extends ResponseStatusException {

  public RestException(HttpStatusCode status, @Nullable String reason) {
    super(status, reason);
  }

  public RestException(HttpStatusCode status, @Nullable String reason, Throwable cause) {
    super(status, reason, cause);
  }
}
