package com.bandall.location_share.web.controller.dto;

import com.bandall.location_share.domain.member.enums.EnumValidation;
import com.bandall.location_share.domain.member.enums.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberLoginDto {
    @EnumValidation(enumClass = LoginType.class)
    private LoginType loginType;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String password;
}
