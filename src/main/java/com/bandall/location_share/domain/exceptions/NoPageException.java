package com.bandall.location_share.domain.exceptions;

public class NoPageException extends RuntimeException {
    public NoPageException() {
    }

    public NoPageException(String message) {
        super(message);
    }

    public NoPageException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoPageException(Throwable cause) {
        super(cause);
    }

    public NoPageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
