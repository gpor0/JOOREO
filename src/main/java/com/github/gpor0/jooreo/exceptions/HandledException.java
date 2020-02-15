package com.github.gpor0.jooreo.exceptions;

/**
 * Author: gpor0
 */
public abstract class HandledException extends RuntimeException {

    public HandledException() {
    }

    public HandledException(String message) {
        super(message);
    }

    public HandledException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandledException(Throwable cause) {
        super(cause);
    }

    public HandledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
