package com.bandall.location_share.domain.member;

import com.bandall.location_share.domain.member.enums.LoginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {

    private final MemberJpaRepository memberRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

        // OAuth2 로그인일 경우 로그인 처리X
        if(member.getLoginType() != LoginType.NONE) {
            log.info("소셜 로그인 유저의 잘못된 로그인 시도");
            throw new UsernameNotFoundException("소셜 로그인된 계정입니다.");
        }

        MemberDetails memberDetails = MemberDetails.builder()
                .id(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .password(member.getPassword())
                .role(member.getRole())
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .isAccountNonLocked(true)
                .authorities(Collections.singleton(new SimpleGrantedAuthority(member.getRole().toString())))
                .build();
//        log.info("memberDetails={}", memberDetails);
        return memberDetails;
    }
}
