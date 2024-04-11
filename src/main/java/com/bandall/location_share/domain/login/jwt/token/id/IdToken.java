package com.bandall.location_share.domain.login.jwt.token.id;

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
@Table(indexes = @Index(name = "idx_id_token_id", columnList = "tokenId"))
public class IdToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String ownerEmail;

    @Column(length = 36, unique = true)
    private String tokenId;

    private Date expireTime;

    private Date lastAccessTime;

    @Builder
    public IdToken(String ownerEmail, String tokenId, Date expireTime) {
        this.ownerEmail = ownerEmail;
        this.tokenId = tokenId;
        this.expireTime = expireTime;
        this.lastAccessTime = new Date();
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = new Date();
    }

}
