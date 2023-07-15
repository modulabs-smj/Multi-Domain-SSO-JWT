package com.bandall.location_share.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class RefreshTokenDto {
    private String email;
    private String tokenId;
    private Date expireTime;
}
