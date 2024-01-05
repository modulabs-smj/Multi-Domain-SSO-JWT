package com.bandall.location_share.domain.admin.dto;

import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.enums.LoginType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfo {

    private Long id;

    private LoginType loginType;

    private String email;

    private boolean isEmailVerified;

    private String username;

    private String profileImageUri;

    private String roles;

    private LocalDateTime lastLoginTime;

    public MemberInfo(Member member) {
        this.id = member.getId();
        this.loginType = member.getLoginType();
        this.email = member.getEmail();
        this.isEmailVerified = member.isEmailVerified();
        this.username = member.getUsername();
        this.profileImageUri = member.getProfileImageUri();
        this.roles = member.getRoles();
        this.lastLoginTime = member.getLastLoginTime();
    }
}
