package com.bandall.location_share.domain.member;

import com.bandall.location_share.domain.login.verification_code.VerificationCode;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.bandall.location_share.domain.member.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"password"})
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(unique = true, length = 50)
    private String email;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private VerificationCode verificationCode;

    private boolean isEmailVerified;

    @Column(length = 100)
    private String password;

    @Column(length = 50)
    private String username;

    @Column(length = 255)
    private String profileImageUri;

    private String roles;

    private LocalDateTime lastLoginTime;

    @Builder
    public Member(LoginType loginType, String email, String password, String username) {
        this.loginType = loginType;
        this.email = email;
        this.isEmailVerified = false;
        this.password = password;
        this.username = username;
        this.roles = Role.ROLE_USER.getRoleName();
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    public void updateVerificationCode(VerificationCode verificationCode) {
        this.verificationCode = verificationCode;
    }

    public void updateEmailVerified(boolean EmailVerified) {
        this.isEmailVerified = EmailVerified;
    }

    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }

    public void addRole(Role role) {
        List<String> parsedRoles = Arrays.stream(roles.split(",")).toList();

        if (parsedRoles.contains(role.getRoleName())) {
            return;
        }

        List<String> newRoles = new ArrayList<>(parsedRoles);
        newRoles.add(role.getRoleName());
        newRoles.sort(Comparator.naturalOrder());

        this.roles = String.join(",", newRoles);
    }

    public void removeRole(Role role) {
        List<String> parsedRoles = Arrays.stream(roles.split(",")).toList();

        if (!parsedRoles.contains(role.getRoleName())) {
            return;
        }

        List<String> newRoles = new ArrayList<>(parsedRoles);
        newRoles.remove(role.getRoleName());
        newRoles.sort(Comparator.naturalOrder());

        this.roles = String.join(",", newRoles);
    }

    public boolean hasRole(Role role) {
        List<String> parsedRoles = Arrays.stream(roles.split(",")).toList();

        return parsedRoles.contains(role.getRoleName());
    }
}
