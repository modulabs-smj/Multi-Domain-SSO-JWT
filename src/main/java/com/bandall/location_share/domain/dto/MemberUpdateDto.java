package com.bandall.location_share.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberUpdateDto {
    public String username;
    public String oldPassword;
    public String newPassword;
}
