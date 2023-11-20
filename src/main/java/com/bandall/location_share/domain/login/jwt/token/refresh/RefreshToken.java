package com.bandall.location_share.domain.login.jwt.token.refresh;

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
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String ownerEmail;

    @Column(length = 36)
    private String tokenId;

    private Date expireTime;

    @Builder
    public RefreshToken(String ownerEmail, String tokenId, Date expireTime) {
        this.ownerEmail = ownerEmail;
        this.tokenId = tokenId;
        this.expireTime = expireTime;
    }
}
