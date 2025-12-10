package com.file.registry.advice;

import com.file.registry.exception.RestException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(value = RestException.class)
    public ProblemDetail handleRestException(
            RestException e,
            HttpServletRequest request) {
        HttpStatus exceptionStatus = (HttpStatus) e.getStatusCode();
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(exceptionStatus, e.getReason());

        problemDetail.setTitle(exceptionStatus.getReasonPhrase());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
