package com.bandall.location_share.domain.admin.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class SessionCountDto {
    private String email;
    private Long sessionCounts;

    @QueryProjection
    public SessionCountDto(String email, Long sessionCounts) {
        this.email = email;
        this.sessionCounts = sessionCounts;
    }
}
