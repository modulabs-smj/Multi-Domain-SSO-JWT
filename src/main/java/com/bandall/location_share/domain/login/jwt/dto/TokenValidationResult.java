package com.bandall.location_share.domain.login.jwt.dto;

import com.bandall.location_share.domain.login.jwt.token.TokenStatus;
import com.bandall.location_share.domain.login.jwt.token.TokenType;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationResult {
    private Boolean result;
    private TokenType tokenType;
    private String tokenId;
    private Claims claims;
    private TokenStatus tokenStatus;

    public String getEmail() {
        return claims.getSubject();
    }
}
