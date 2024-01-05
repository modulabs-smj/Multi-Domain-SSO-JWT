package com.bandall.location_share.domain.admin.dto;

import com.bandall.location_share.domain.member.enums.Role;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
public class RoleInfo {
    List<String> roles;

    public RoleInfo() {
        roles = Arrays.stream(Role.values())
                .map(Objects::toString)
                .toList();
    }
    
}
