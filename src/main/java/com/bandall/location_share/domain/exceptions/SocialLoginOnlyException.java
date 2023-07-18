package com.bandall.location_share.domain.exceptions;

public class SocialLoginOnlyException extends RuntimeException{
    public SocialLoginOnlyException(String message) {
        super(message);
    }

    public SocialLoginOnlyException(String message, Throwable cause) {
        super(message, cause);
    }
}
