package com.bandall.location_share.domain.login.oauth2.userinfo;

import lombok.ToString;

import java.util.Map;

public class KakaoUserInfo extends OAuth2UserInfo {
    private Long oAuth2Id;

    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
        this.oAuth2Id = (Long) attributes.get("id");
        this.email = (String) ((Map<String, Object>) attributes.get("kakao_account")).get("email");
        this.name = (String) ((Map<String, Object>) attributes.get("properties")).get("nickname");
        this.profileImageUri = (String) ((Map<String, Object>) attributes.get("properties")).get("thumbnail_image");
        this.isEmailVerified = (Boolean) ((Map<String, Object>) attributes.get("kakao_account")).get("is_email_verified");
    }

    @Override
    public String getOAuth2Id() {
        return oAuth2Id.toString();
    }

    @Override
    public String getEmail() {
        return email;
    }

    public Boolean isEmailVerified() {
        return isEmailVerified;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProfileImageUri() {
        return profileImageUri;
    }

    @Override
    public String toString() {
        return "KakaoUserInfo(" +
                "id=" + oAuth2Id +
                ", email=" + email +
                ", name=" + name +
                ", isEmailVerified=" + isEmailVerified +
                ", profileImage=" + profileImageUri +
                ')';
    }
}
