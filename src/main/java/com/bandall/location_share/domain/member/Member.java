package com.bandall.location_share.domain.member;

import com.bandall.location_share.domain.member.enums.Role;
import com.bandall.location_share.domain.member.enums.LoginType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"password"})
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(unique = true, length = 50)
    private String email;

    private boolean isEmailVerified;

    @Column(length = 100)
    private String password;

    @Column(length = 50)
    private String username;

    @Column(length = 255)
    private String profileImageUri;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime lastAccessedTime;

    public Member updatePassword(String password) {
        this.password = password;
        return this;
    }

    public Member updateUsername(String username) {
        this.username = username;
        return this;
    }

    public Member updateProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
        return this;
    }

    public void updateEmailVerified(boolean EmailVerified) {
        this.isEmailVerified = EmailVerified;
    }

    @Builder
    public Member(LoginType loginType, String email, String password, String username, Role role) {
        this.loginType = loginType;
        this.email = email;
        this.isEmailVerified = false;
        this.password = password;
        this.username = username;
        this.role = role;
    }
}
