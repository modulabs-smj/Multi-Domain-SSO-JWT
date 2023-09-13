package com.bandall.location_share.domain.login;

import com.bandall.location_share.web.controller.dto.MemberCreateDto;
import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.exceptions.EmailNotVerified;
import com.bandall.location_share.domain.login.jwt.token.*;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshToken;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshTokenRepository;
import com.bandall.location_share.domain.login.verification_code.EmailVerificationService;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberDetails;
import com.bandall.location_share.domain.member.MemberJpaRepository;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.bandall.location_share.domain.member.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {

    private final MemberJpaRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final RedisAccessTokenBlackListRepository blackListRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationService verificationService;

    // 테스트 시 이메일 인증 OFF
    @Value("${verification.email.do-email-verification}")
    private boolean doEmailVerification;

    private static final String PASSWORD_REGEX_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$";

    public Member createMember(MemberCreateDto memberCreateDto) {
        if(followingPasswordStrategy(memberCreateDto.getPassword())) {
            log.info("비밀번호 정책 미달");
            throw new IllegalArgumentException("비밀번호는 최소 8자리에 영어, 숫자, 특수문자를 포함해야 합니다.");
        }

        if(memberRepository.existsByEmail(memberCreateDto.getEmail())) {
            log.info("이미 등록된 이메일={}", memberCreateDto.getEmail());
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        Member member = Member.builder()
                .loginType(LoginType.EMAIL_PW)
                .email(memberCreateDto.getEmail())
                .password(passwordEncoder.encode(memberCreateDto.getPassword()))
                .username(memberCreateDto.getUsername())
                .role(Role.ROLE_USER).build();

        return memberRepository.save(member);
    }

    // OAuth 적용 시 EMAIL_PW 타입만 사용 가능하도록 수정 + Email 인증 확인 로직 추가
    public TokenInfoDto loginMember(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        try {
             // 이메일 비밀번호 확인 후 authentication 생성
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 이메일 인증이 안되어 있을 경우 (트랜잭션 전파로 인해 다른 클래스에 역할 위임)
            if(!((MemberDetails) authentication.getPrincipal()).isEmailVerified() && doEmailVerification) {
                verificationService.sendVerificationEmail(email);
                throw new EmailNotVerified("이메일 인증이 되어 있지 않습니다. [" + email + "]로 보낸 메일을 통해 인증을 진행해 주세요.", email);
            }

            // 생성된 authentication 정보를 토대로 access, refresh 토큰 생성
            TokenInfoDto tokenInfoDto = tokenProvider.createTokenWithAuthentication(authentication);

            RefreshToken refreshToken = RefreshToken.builder()
                    .ownerEmail(tokenInfoDto.getOwnerEmail())
                    .value(tokenInfoDto.getRefreshToken())
                    .expireTime(tokenInfoDto.getRefreshTokenExpireTime())
                    .build();

            refreshTokenRepository.save(refreshToken);
            return tokenInfoDto;
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("계정이 존재하지 않거나 비밀번호가 잘못되었습니다. 소셜 로그인의 경우 소셜 로그인을 이용해주세요.");
        }
    }

    public TokenInfoDto refreshToken(String accessToken, String refreshToken) {
        TokenValidationResult validationResult = tokenProvider.isAccessTokenAndRefreshTokenValid(accessToken, refreshToken);
        // 1. validateToken에 tokenId 값 추가해서 검사 -> 토큰 교차 방지 [굳이 필요한지는 모르겠음(허점이긴 하나 활용할 수 있는 공격기법이 없음)]
        // 2. tokenId를 통해 blackList 없이 검증 가능 + db에 refresh 토큰 value를 저장하지 않아도 됨(tokenID만 저장)
        switch (validationResult.getTokenStatus()) {
            case TOKEN_EXPIRED -> throw new IllegalArgumentException("만료된 Refresh 토큰입니다.");
            case TOKEN_WRONG_SIGNATURE -> throw new IllegalArgumentException("잘못된 토큰입니다.");
            case TOKEN_IS_BLACKLIST -> throw new IllegalArgumentException("폐기된 Access 토큰입니다.");
            case TOKEN_ID_NOT_MATCH -> throw new IllegalArgumentException("잘못된 토큰 쌍입니다.");
        }

        RefreshToken refreshTokenDb = refreshTokenRepository.findRefreshTokenByValue(refreshToken).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 Refresh 토큰입니다."));

        String email = refreshTokenDb.getOwnerEmail();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> {
            log.error("계정이 존재하지 않음");
            return new IllegalArgumentException("계정이 존재하지 않습니다.");
        });

        // 정책에 따라서 이전 리프레시 토큰의 만료 시간을 그대로 유지할 수도있음(주기적 로그인)
        TokenInfoDto tokenInfoDto = tokenProvider.createTokenWithMember(member);
        RefreshToken newRefreshToken = RefreshToken.builder()
                .ownerEmail(tokenInfoDto.getOwnerEmail())
                .value(tokenInfoDto.getRefreshToken())
                .expireTime(tokenInfoDto.getRefreshTokenExpireTime())
                .build();

        refreshTokenRepository.save(newRefreshToken);
        refreshTokenRepository.delete(refreshTokenDb);
        blackListRepository.setBlackList(accessToken, email);
        return tokenInfoDto;
    }

    public void logout(String email, String accessToken, String refreshToken) {
        refreshTokenRepository.findRefreshTokenByValue(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 Refresh 토큰입니다."));

        refreshTokenRepository.deleteRefreshTokenByValue(refreshToken);
        blackListRepository.setBlackList(accessToken, email);
    }

    public Member updateUsername(String email, String username) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> {
            log.error("계정이 존재하지 않음");
            return new IllegalArgumentException("계정이 존재하지 않음");
        });

        member.updateUsername(username);
        return member;
    }

    public Member updatePassword(String email, String newPassword, String oldPassword) {
        if(followingPasswordStrategy(newPassword)) {
            log.info("비밀번호 정책 미달");
            throw new IllegalArgumentException("새 비밀번호는 최소 8자리에 영어, 숫자, 특수문자를 포함해야 합니다.");
        }

        Member member = memberRepository.findByEmail(email).orElseThrow(() -> {
            log.error("계정이 존재하지 않음");
            return new IllegalArgumentException("계정이 존재하지 않습니다.");
        });

        if(member.getLoginType() != LoginType.EMAIL_PW) {
            log.info("소셜 로그인 유저의 잘못된 비밀번호 변경 요청");
            throw new BadCredentialsException("소셜 로그인 서비스를 사용 중인 계정으로 비밀번호가 존재하지 않습니다.");
        }

        if(!passwordEncoder.matches(oldPassword, member.getPassword())) {
            log.info("일치하지 않는 비밀번호");
            throw new BadCredentialsException("기존 비밀번호 확인에 실패했습니다.");
        }

        member.updatePassword(passwordEncoder.encode(newPassword));
        return member;
    }

    public void deleteMember(String email, String password, String accessToken) {
        Member foundMember = memberRepository.findByEmail(email).orElseThrow(() -> {
            log.error("이미 삭제되거나 존재하지 않는 계정");
            return new IllegalArgumentException("이미 삭제되거나 존재하지 않는 계정입니다.");
        });

        if(!passwordEncoder.matches(password, foundMember.getPassword())) {
            log.info("일치하지 않는 비밀번호");
            throw new BadCredentialsException("비밀번호 확인에 실패했습니다.");
        }

        refreshTokenRepository.deleteAllRefreshTokenByEmail(email);
        memberRepository.deleteById(foundMember.getId());
        memberRepository.flush();
        blackListRepository.setBlackList(accessToken, email);
    }

    public void verifyEmail(String email, String code) {
        verificationService.verifyEmail(email, code);
    }

    public void sendVerifyEmail(String email) {
        verificationService.sendVerificationEmail(email);
    }

    public Map<String, String> getUserInfo(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(RuntimeException::new);

        HashMap<String, String> map = new HashMap<>();
        map.put("username", member.getUsername());
        map.put("email", member.getEmail());
        map.put("profileImageUrl", member.getProfileImageUri());
        return map;
    }

    //패스워드 설정 정책
    private boolean followingPasswordStrategy(String password) {
        return !Pattern.matches(PASSWORD_REGEX_PATTERN, password);
    }
}
