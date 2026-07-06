package com.prakash.clinicos.exception;

import org.springframework.http.HttpStatus;

/**
 * Business exception that carries an HTTP status code.
 *
 * Why not use raw RuntimeException?
 * RuntimeException doesn't carry an HTTP status, so the global handler
 * can't know whether to return 404, 409, 401, etc. AppException makes
 * the intent explicit at the throw site:
 *
 *   throw new AppException(HttpStatus.CONFLICT, "Email already registered");
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
