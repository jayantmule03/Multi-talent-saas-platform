package com.multitalent.common.exception;

/**
 * Raised when a service-to-service call (e.g. auth-service -> tenant-service)
 * fails or the upstream service returns an unexpected response.
 */
public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message) {
        super(message);
    }

    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
