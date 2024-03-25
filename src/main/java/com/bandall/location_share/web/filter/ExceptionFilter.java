package com.bandall.location_share.web.filter;

import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 스프링 시큐리티 사용으로 인해 서블릿부터 올라오는 예외를 처리하는 필터
 * 서블릿은 스프링 ExceptionAdvice 대상이 아니기 때문에 스프링 시큐리티 앞에 필터를 붙어준다.
 */
@Slf4j
@Component
@Order(SecurityProperties.DEFAULT_FILTER_ORDER - 1)
public class ExceptionFilter extends OncePerRequestFilter {
    private static final String INTERNAL_SERVER_ERROR = "Unexpected Server Error";
    private static final String ERROR_MESSAGE_KEY = "errMsg";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            doFilter(request, response, filterChain);
        } catch (Exception e) {
            log.error(INTERNAL_SERVER_ERROR, e);
            sendError(response, INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
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
