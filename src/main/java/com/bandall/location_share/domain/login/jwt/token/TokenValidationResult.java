package com.bandall.location_share.domain.login.jwt.token;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationResult {
    private Boolean result;
    private TokenType tokenType;
    private TokenStatus tokenStatus;
    private Exception exception;
}
