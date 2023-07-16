package com.bandall.location_share.domain.member;

import com.bandall.location_share.domain.member.enums.Role;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@Builder
public class MemberDetails implements UserDetails {
    private Long id;
    private String email;
    private String username;
    private String password;
    private Role role;

    private boolean isEnabled;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private Collection<? extends GrantedAuthority> authorities;
}