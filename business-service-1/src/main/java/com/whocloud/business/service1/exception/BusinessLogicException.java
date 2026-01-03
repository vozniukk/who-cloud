package com.whocloud.business.service1.exception;

/**
 * Exception thrown when business logic validation fails.
 * Results in HTTP 400 Bad Request response.
 */
public class BusinessLogicException extends RuntimeException {
    
    public BusinessLogicException(String message) {
        super(message);
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}
