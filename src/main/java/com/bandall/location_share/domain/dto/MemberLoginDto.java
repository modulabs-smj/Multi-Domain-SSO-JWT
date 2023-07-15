package com.bandall.location_share.domain.dto;

import com.bandall.location_share.domain.member.enums.EnumValidation;
import com.bandall.location_share.domain.member.enums.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberLoginDto {
    @EnumValidation(enumClass = LoginType.class)
    private LoginType loginType;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String password;
}
