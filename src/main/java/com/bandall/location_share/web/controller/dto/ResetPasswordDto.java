package com.bandall.location_share.web.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResetPasswordDto {
    @Email
    String email;
    @NotEmpty
    String code;
    @NotEmpty
    String newPassword;
}
