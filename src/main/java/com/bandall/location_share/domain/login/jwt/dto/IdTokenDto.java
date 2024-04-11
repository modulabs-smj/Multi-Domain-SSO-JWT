package com.bandall.location_share.domain.login.jwt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@ToString(exclude = "idToken")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdTokenDto {
    @NotNull
    private String idToken;
    private Date idTokenExpireTime;
    private String ownerEmail;
    private String tokenId;

    @Builder
    public IdTokenDto(String idToken, Date idTokenExpireTime, String ownerEmail, String tokenId) {
        this.idToken = idToken;
        this.idTokenExpireTime = idTokenExpireTime;
        this.ownerEmail = ownerEmail;
        this.tokenId = tokenId;
    }
}
