package com.bandall.location_share.domain.login.jwt;

import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.ResponseStatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.Map;

public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final String ERROR_MESSAGE_KEY = "errMsg";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        sendError(response, "접근권한이 없습니다.", ResponseStatusCode.FORBIDDEN);
    }

    private void sendError(HttpServletResponse response, String msg, int code) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ApiResponseJson responseJson = new ApiResponseJson(HttpStatus.valueOf(HttpServletResponse.SC_FORBIDDEN), code, Map.of(ERROR_MESSAGE_KEY, msg));

        String jsonToString = objectMapper.writeValueAsString(responseJson);
        response.getWriter().write(jsonToString);
    }
}
