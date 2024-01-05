package com.bandall.location_share.web.controller.dto;

import com.bandall.location_share.domain.member.enums.EnumValidation;
import com.bandall.location_share.domain.member.enums.Role;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleModifyDto {

    @Email
    private String email;

    @EnumValidation(enumClass = RoleAction.class)
    private RoleAction action;

    @EnumValidation(enumClass = Role.class)
    private Role role;

    public enum RoleAction {
        ADD_ROLE, REMOVE_ROLE
    }
}
