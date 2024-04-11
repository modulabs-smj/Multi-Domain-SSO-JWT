package com.bandall.location_share.domain.login.jwt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@ToString(exclude = {"accessToken", "refreshToken"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessRefreshTokenDto {
    // 아래 2개 필드는 controller에서 전달 받을 때 사용
    @NotNull
    private String accessToken;
    @NotNull
    private String refreshToken;

    //아래 필드는 서버 내부에서 토큰 전달 시 사용
    private Date accessTokenExpireTime;
    private Date refreshTokenExpireTime;
    private String ownerEmail;
    private String tokenId;

    @Builder
    public AccessRefreshTokenDto(String accessToken, String refreshToken, Date accessTokenExpireTime, Date refreshTokenExpireTime, String ownerEmail, String tokenId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
        this.ownerEmail = ownerEmail;
        this.tokenId = tokenId;
    }
}
