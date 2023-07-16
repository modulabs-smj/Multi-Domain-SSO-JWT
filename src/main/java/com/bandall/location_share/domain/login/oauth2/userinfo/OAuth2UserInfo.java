package com.bandall.location_share.domain.login.oauth2.userinfo;

import lombok.Getter;
import java.util.Map;

@Getter
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;
    protected String email;
    protected String name;
    protected String profileImageUri;
    protected Boolean isEmailVerified;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getOAuth2Id();
    public abstract String getEmail();
    public abstract String getName();
    public abstract String getProfileImageUri();
    public abstract Boolean isEmailVerified();
}
