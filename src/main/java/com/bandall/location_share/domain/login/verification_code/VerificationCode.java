package com.bandall.location_share.domain.login.verification_code;

import com.bandall.location_share.domain.member.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationCode extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String email;

    @Column(length = 40)
    private String verificationCode;

    Date expireTime;

    @Builder
    public VerificationCode(String email, String verificationCode, Date expireTime) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.expireTime = expireTime;
    }
}
