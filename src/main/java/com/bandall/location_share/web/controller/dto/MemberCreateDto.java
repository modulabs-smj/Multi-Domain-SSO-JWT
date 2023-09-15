package com.bandall.location_share.web.controller.dto;

import com.bandall.location_share.domain.member.enums.EnumValidation;
import com.bandall.location_share.domain.member.enums.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCreateDto {
//    @EnumValidation(enumClass = LoginType.class)
//    private LoginType loginType;

    @Email
    private String email;

    @NotNull
    private String password;

    @NotEmpty
    private String username;
}
