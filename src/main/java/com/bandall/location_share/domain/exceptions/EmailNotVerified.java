package com.bandall.location_share.domain.exceptions;

public class EmailNotVerified extends RuntimeException{

    private String email;

    public String getEmail() {
        return email;
    }

    public EmailNotVerified(String message, String email) {
        super(message);
        this.email = email;
    }

    public EmailNotVerified(String message, Throwable cause) {
        super(message, cause);
    }
}
