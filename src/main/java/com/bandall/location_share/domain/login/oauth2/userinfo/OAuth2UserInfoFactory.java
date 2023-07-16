package com.bandall.location_share.domain.login.oauth2.userinfo;

import com.bandall.location_share.domain.member.enums.LoginType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.AuthProvider;
import java.util.Map;

@Slf4j
@Component
public class OAuth2UserInfoFactory {
    @Value("${spring.oauth2.client.provider.kakao.authorization-uri}")
    private String kakaoAuthUri;

    @Value("${spring.oauth2.client.provider.kakao.unlink-uri}")
    private String kakaoUnlinkUri;

    public OAuth2UserInfo getOAuth2UserInfo(LoginType loginType, Map<String, Object> attributes) {
        switch (loginType) {
            case KAKAO: return new KakaoUserInfo(attributes);
            default: throw new IllegalArgumentException("Invalid Provider Type");
        }
    }

    public String getAuthorizationUri(LoginType loginType) {
        switch (loginType) {
            case KAKAO: return kakaoAuthUri;
            default: throw new IllegalArgumentException("Invalid Provider Type");
        }
    }

    public String getUnlinkUri(LoginType loginType) {
        switch (loginType) {
            case KAKAO: return kakaoUnlinkUri;
            default: throw new IllegalArgumentException("Invalid Provider Type");
        }
    }
}
