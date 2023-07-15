package com.bandall.location_share.domain.dto;

import com.bandall.location_share.domain.member.enums.EnumValidation;
import com.bandall.location_share.domain.member.enums.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberCreateDto {
    @EnumValidation(enumClass = LoginType.class)
    private LoginType loginType;

    @Email
    private String email;

    @NotNull
    private String password;

    @NotEmpty
    private String username;
}
