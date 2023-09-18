package com.bandall.location_share.domain.login.verification_code;

import com.bandall.location_share.domain.member.BaseTimeEntity;
import com.bandall.location_share.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationCode extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40)
    private String code;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    LocalDateTime expireTime;

    @Builder
    public VerificationCode(Member member, String verificationCode, LocalDateTime expireTime) {
        this.member = member;
        this.code = verificationCode;
        this.expireTime = expireTime;
    }
}
