package com.bandall.location_share.domain.exceptions;

public class EmailNotVerified extends RuntimeException{
    public EmailNotVerified(String message) {
        super(message);
    }

    public EmailNotVerified(String message, Throwable cause) {
        super(message, cause);
    }
}
