package com.bandall.location_share.domain.exceptions;

public class EmailNotVerifiedException extends RuntimeException{

    private String email;

    public String getEmail() {
        return email;
    }

    public EmailNotVerifiedException(String message, String email) {
        super(message);
        this.email = email;
    }

    public EmailNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
