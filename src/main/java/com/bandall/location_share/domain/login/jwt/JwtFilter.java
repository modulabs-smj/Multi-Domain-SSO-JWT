package com.bandall.location_share.domain.login.jwt;


import com.bandall.location_share.domain.exceptions.DiscardedJwtException;
import com.bandall.location_share.domain.login.jwt.token.TokenProvider;
import com.bandall.location_share.domain.login.jwt.token.TokenStatus;
import com.bandall.location_share.domain.login.jwt.token.TokenType;
import com.bandall.location_share.domain.login.jwt.token.TokenValidationResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if(StringUtils.hasText(token)) {
            // 토큰 유효성 검사
            TokenValidationResult tokenValidationResult = tokenProvider.validateToken(token);
            boolean isBlackList = tokenProvider.isAccessTokenBlackList(token);

            if(tokenValidationResult.getResult() && !isBlackList && tokenValidationResult.getTokenType() == TokenType.ACCESS) {
                // 1. 정상적인 토큰인 경우
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("AUTH SUCCESS : {},", authentication.getName());
            } else {
                // 2. 잘못된 토큰일 경우
                // jwt 토큰 예외 구분 처리를 위해 request에 tokenValidationResult를 담아 EntryPoint에 전달
                if(isBlackList) {
                    tokenValidationResult.setResult(false);
                    tokenValidationResult.setTokenStatus(TokenStatus.TOKEN_IS_BLACKLIST);
                    tokenValidationResult.setException(new DiscardedJwtException());
                }
                request.setAttribute("result", tokenValidationResult);
            }
        } else {
            // 3. Authorization 헤더에 refresh 토큰을 담은 경우
            log.info("Refresh Token in Authorization Header");
            request.setAttribute("result",
                    new TokenValidationResult(false, null, TokenStatus.NO_AUTH_HEADER, null)
            );
        }

        filterChain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
