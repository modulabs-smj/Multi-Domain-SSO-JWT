package com.bandall.location_share.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
public class TokenInfoDto {
    @NotNull
    private String accessToken;
    @NotNull
    private String refreshToken;

    @Builder
    public TokenInfoDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
