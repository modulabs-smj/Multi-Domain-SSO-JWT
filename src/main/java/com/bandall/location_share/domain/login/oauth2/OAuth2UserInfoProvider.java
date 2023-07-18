package com.bandall.location_share.domain.login.oauth2;

import com.bandall.location_share.domain.exceptions.BadResponseException;
import com.bandall.location_share.domain.login.oauth2.userinfo.KakaoUserInfo;
import com.bandall.location_share.domain.login.oauth2.userinfo.OAuth2UserInfo;
import com.bandall.location_share.domain.login.oauth2.userinfo.OAuth2UserInfoFactory;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2UserInfoProvider {

    private final OAuth2UserInfoFactory oAuth2UserInfoFactory;
    private WebClient webClient;
    private ObjectMapper mapper;

    @PostConstruct
    public void init() {
        webClient = WebClient.builder().build();
        mapper =  new ObjectMapper();
    }

    public OAuth2UserInfo getProfile(String accessToken, LoginType loginType) {
        ResponseEntity<String> response = null;
        try {
            response = webClient.get()
                    .uri(oAuth2UserInfoFactory.getAuthorizationUri(loginType))
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if(statusCode.is4xxClientError()) {
                log.info("잘못된 OAuth2 Access 토큰");
                throw new BadResponseException("잘못된 소셜 로그인 Access 토큰입니다.");
            }
            if(statusCode.is5xxServerError()) {
                log.error("OAuth 서버에 오류 발생 => {}", e.getMessage());
                throw new BadResponseException("OAuth2 서버에 오류가 발생했습니다.", e);
            }
            throw new RuntimeException("소셜 로그인 정보 조회 중 오류 발생", e);
        }

        try {
            Map<String, Object> attributes = mapper.readValue(response.getBody(), Map.class);
            return oAuth2UserInfoFactory.getOAuth2UserInfo(loginType, attributes);
        } catch (JsonProcessingException | NullPointerException e) {
            throw new RuntimeException("소셜 로그인 사용자 정보 JSON 파싱 중 오류 발생", e);
        }
    }

    public void unlinkSocial(String accessToken, LoginType loginType) {
        ResponseEntity<String> response = null;
        try {
            response = webClient.get()
                    .uri(oAuth2UserInfoFactory.getUnlinkUri(loginType))
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if(statusCode.is4xxClientError()) {
                log.info("잘못된 OAuth2 Access 토큰");
                throw new BadResponseException("잘못된 OAuth2 Access 토큰입니다.");
            }
            if(statusCode.is5xxServerError()) {
                log.error("OAuth 서버에 오류 발생 => {}", e.getMessage());
                throw new BadResponseException("OAuth2 서버에 오류가 발생했습니다.", e);
            }
            throw new RuntimeException("OAuth2 정보 조회 중 오류 발생", e);
        }

        if(response.getStatusCode() != HttpStatus.OK)
            throw new BadResponseException("소셜 로그인 연동 해제에 실패했습니다.");
    }
}
