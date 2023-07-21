package com.bandall.location_share.domain.login.jwt;

import com.bandall.location_share.domain.login.jwt.token.TokenValidationResult;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.Code;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;


import java.io.IOException;
import java.util.Map;

@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // message 전송 위해 server.error.include-message=always 설정
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // request에 담아둔 TokenValidationResult를 이용해 예외를 구분해 처리
        TokenValidationResult result = (TokenValidationResult) request.getAttribute("result");
        switch (result.getTokenStatus()) {
            case TOKEN_EXPIRED -> sendError(response, "Access Token Expired", Code.TOKEN_EXPIRED);
            case TOKEN_IS_BLACKLIST -> sendError(response, "Access Token Discarded", Code.TOKEN_IS_BLACKLIST);
            case TOKEN_WRONG_SIGNATURE -> sendError(response, "Wrong Access Token", Code.TOKEN_WRONG_SIGNATURE);
            case TOKEN_HASH_NOT_SUPPORTED -> sendError(response, "Access Token Unsupported", Code.TOKEN_HASH_NOT_SUPPORTED);
            case NO_AUTH_HEADER -> sendError(response, "No Authorization Header", Code.NO_AUTH_HEADER);
            case TOKEN_VALIDATION_TRY_FAILED -> {
                log.error("Error while validating token", result.getException());
                sendError(response, "Wrong Authentication", Code.TOKEN_VALIDATION_TRY_FAILED);
            }
            default -> sendError(response, "Wrong Authentication", Code.TOKEN_VALIDATION_TRY_FAILED);
        }
    }

    private void sendError(HttpServletResponse response, String msg, int code) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ApiResponseJson responseJson = new ApiResponseJson(HttpStatus.valueOf(HttpServletResponse.SC_UNAUTHORIZED), code, Map.of("errMsg", msg));

        String jsonToString = objectMapper.writeValueAsString(responseJson);
        response.getWriter().write(jsonToString);
    }
}
