package com.bandall.location_share.domain.exceptions;

public class IdTokenNotValidException extends RuntimeException {
    public IdTokenNotValidException() {
    }

    public IdTokenNotValidException(String message) {
        super(message);
    }
}
