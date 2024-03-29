package com.bandall.location_share.config;

import com.bandall.location_share.domain.login.jwt.JwtAccessDeniedHandler;
import com.bandall.location_share.domain.login.jwt.JwtAuthenticationEntryPoint;
import com.bandall.location_share.domain.login.jwt.JwtProperties;
import com.bandall.location_share.domain.login.jwt.token.TokenProvider;
import com.bandall.location_share.domain.login.jwt.token.access.RedisAccessTokenBlackListRepository;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private final RedisAccessTokenBlackListRepository blackListRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    @Bean
    public TokenProvider tokenProvider(JwtProperties jwtProperties) {
        return new TokenProvider(jwtProperties.getSecret(),
                jwtProperties.getAccessTokenValidityInSeconds(),
                jwtProperties.getRefreshTokenValidityInSeconds(),
                refreshTokenRepository,
                blackListRepository);
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }
}
