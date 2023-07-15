package com.bandall.location_share.domain.login.jwt;

import com.bandall.location_share.domain.login.jwt.token.TokenValidationResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;


import java.io.IOException;

@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // message 전송 위해 server.error.include-message=always 설정
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // request에 담아둔 TokenValidationResult를 이용해 예외를 구분해 처리
        TokenValidationResult result = (TokenValidationResult) request.getAttribute("result");
        switch (result.getTokenStatus()) {
            case TOKEN_EXPIRED -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired");
            case TOKEN_IS_BLACKLIST -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Discarded");
            case TOKEN_WRONG_SIGNATURE -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "WrongToken");
            case TOKEN_HASH_NOT_SUPPORTED -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported");
            case NO_AUTH_HEADER -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No Authorization Header");
            case TOKEN_VALIDATION_TRY_FAILED -> {
                log.error("Error while validating token", result.getException());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "WrongAuthentication");
            }
            default -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "WrongAuthentication");
        }
    }
}
