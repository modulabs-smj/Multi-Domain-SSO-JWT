package com.bandall.location_share.domain.member;

import com.bandall.location_share.domain.exceptions.SocialLoginOnlyException;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {

    private final MemberJpaRepository memberRepository;

    /**
     * 로그인 시 jwt 발급에만 사용하는 함수.
     * MemberDetails에 member 객체의 데이터를 모두 넘기지 않아도 된다.
     */
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
                .isEmailVerified(member.isEmailVerified())
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .isAccountNonLocked(true)
                .authorities(Collections.singleton(new SimpleGrantedAuthority(member.getRole().toString())))
                .build();
        return memberDetails;
    }
}
