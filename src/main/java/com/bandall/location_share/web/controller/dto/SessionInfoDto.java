package com.bandall.location_share.web.controller.dto;

import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionInfoDto {

    private String email;

    private String tokenId;

    private LocalDateTime createdDate;

    private Date expireDate;

    private Long duplicatedSessionCounts;

    public SessionInfoDto(RefreshToken refreshToken, Long duplicatedSessionCounts) {
        this.email = refreshToken.getOwnerEmail();
        this.tokenId = refreshToken.getTokenId();
        this.createdDate = refreshToken.getCreatedDate();
        this.expireDate = refreshToken.getExpireTime();
        this.duplicatedSessionCounts = duplicatedSessionCounts;
    }

}
