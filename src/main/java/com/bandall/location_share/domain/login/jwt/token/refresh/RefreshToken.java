package com.bandall.location_share.domain.login.jwt.token.refresh;

import com.bandall.location_share.domain.member.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@ToString(exclude = "value")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String ownerEmail;

    @Column(length = 300)
    private String value;

    private Date expireTime;

    @Builder
    public RefreshToken(String ownerEmail, String value, Date expireTime) {
        this.ownerEmail = ownerEmail;
        this.value = value;
        this.expireTime = expireTime;
    }
}
