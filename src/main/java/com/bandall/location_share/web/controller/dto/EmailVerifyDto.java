package com.bandall.location_share.web.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class EmailVerifyDto {
    @NotEmpty
    String email;
    @NotEmpty
    String code;
}
