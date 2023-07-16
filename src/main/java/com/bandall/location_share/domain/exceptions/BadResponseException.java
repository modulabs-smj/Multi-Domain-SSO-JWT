package com.bandall.location_share.domain.exceptions;

public class BadResponseException extends RuntimeException{
    public BadResponseException(String message) {
        super(message);
    }

    public BadResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
