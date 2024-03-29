package com.bandall.location_share.domain.member;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// TODO: UserPrinciple -> MemberDetails로 마이그레이션 하기
@Getter
public class MemberDetails implements UserDetails {

    private final String email;
    private final String username;
    private final String tokenId;
    private final Collection<? extends GrantedAuthority> authorities;

    public MemberDetails(String email, String username, String tokenId,
                             Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.username = username;
        this.tokenId = tokenId;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "[PASSWORD-EMPTY]";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
