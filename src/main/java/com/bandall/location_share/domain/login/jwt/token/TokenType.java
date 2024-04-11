package com.bandall.location_share.domain.login.jwt.token;

public enum TokenType {
    ACCESS, REFRESH, ID;

    public static TokenType fromString(String tokenType) {
        return switch (tokenType) {
            case "ACCESS" -> ACCESS;
            case "REFRESH" -> REFRESH;
            case "ID" -> ID;
            default -> throw new IllegalArgumentException("Invalid token type");
        };
    }
}
