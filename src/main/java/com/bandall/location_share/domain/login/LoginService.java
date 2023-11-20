package com.bandall.location_share.domain.login;

import com.bandall.location_share.domain.exceptions.EmailNotVerifiedException;
import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.login.jwt.token.*;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshToken;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshTokenRepository;
import com.bandall.location_share.domain.login.verification_code.EmailVerificationService;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberJpaRepository;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.bandall.location_share.domain.member.enums.Role;
import com.bandall.location_share.web.controller.dto.MemberCreateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private final MemberJpaRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisAccessTokenBlackListRepository blackListRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationService verificationService;
    // 테스트 시 이메일 인증 OFF
    @Value("${verification.email.do-email-verification}")
    private boolean doEmailVerification;

    public Member createMember(MemberCreateDto memberCreateDto) {
        checkPasswordStrength(memberCreateDto.getPassword());

        if (memberRepository.existsByEmail(memberCreateDto.getEmail())) {
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

    public TokenInfoDto loginMember(String email, String password) {
        try {
            Member member = findMemberByEmail(email);
            isSocialLogin(member, "소셜 로그인 계정입니다.");
            checkPassword(password, member);

            if (!member.isEmailVerified() && doEmailVerification) {
                verificationService.sendVerificationEmail(email);
                throw new EmailNotVerifiedException("이메일 인증이 되어 있지 않습니다. [" + email + "]로 보낸 메일을 통해 인증을 진행해 주세요.", email);
            }

            TokenInfoDto tokenInfoDto = tokenProvider.createToken(member);
            RefreshToken refreshToken = RefreshToken.builder()
                    .ownerEmail(tokenInfoDto.getOwnerEmail())
                    .tokenId(tokenInfoDto.getTokenId())
                    .expireTime(tokenInfoDto.getRefreshTokenExpireTime())
                    .build();
            refreshTokenRepository.save(refreshToken);

            return tokenInfoDto;
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("계정이 존재하지 않거나 비밀번호가 잘못되었습니다. 소셜 로그인의 경우 소셜 로그인을 이용해주세요.");
        }
    }
    
    public TokenInfoDto refreshToken(String accessToken, String refreshToken) {
        TokenValidationResult validationResult = tokenProvider.isAccessAndRefreshTokenValid(accessToken, refreshToken);
        // 1. validateToken에 tokenId 값 추가해서 검사
        // 2. tokenId를 통해 blackList 없이 검증 가능 + db에 refresh 토큰 value를 저장하지 않아도 됨(tokenID만 저장)
        switch (validationResult.getTokenStatus()) {
            case TOKEN_EXPIRED ->
                    throw new IllegalArgumentException(TokenStatus.TOKEN_EXPIRED.getMessageKr(TokenType.REFRESH));
            case TOKEN_IS_BLACKLIST ->
                    throw new IllegalArgumentException(TokenStatus.TOKEN_IS_BLACKLIST.getMessageKr(TokenType.ACCESS));
            case TOKEN_WRONG_SIGNATURE ->
                    throw new IllegalArgumentException(TokenStatus.TOKEN_WRONG_SIGNATURE.getMessageKr(null));
            case TOKEN_ID_NOT_MATCH ->
                    throw new IllegalArgumentException(TokenStatus.TOKEN_ID_NOT_MATCH.getMessageKr(null));
        }

        String tokenId = validationResult.getTokenId();
        String email = validationResult.getEmail();

        checkRefreshTokenExists(tokenId, email);

        Member member = findMemberByEmail(email);

        // 정책에 따라서 이전 리프레시 토큰의 만료 시간을 그대로 유지할 수도있음(주기적 로그인)
        TokenInfoDto tokenInfoDto = tokenProvider.createToken(member);
        RefreshToken newRefreshToken = RefreshToken.builder()
                .ownerEmail(tokenInfoDto.getOwnerEmail())
                .tokenId(tokenInfoDto.getTokenId())
                .expireTime(tokenInfoDto.getRefreshTokenExpireTime())
                .build();

        refreshTokenRepository.deleteRefreshTokenByTokenIdAndOwnerEmail(tokenId, email);
        refreshTokenRepository.save(newRefreshToken);
        blackListRepository.setBlackList(accessToken, email);
        return tokenInfoDto;
    }

    public void logout(String email, String accessToken, String refreshToken) {
        TokenValidationResult validationResult = tokenProvider.validateToken(refreshToken);

        if (!validationResult.getResult()) {
            throw new IllegalArgumentException("존재하지 않는 Refresh 토큰입니다.");
        }

        String tokenId = validationResult.getTokenId();
        refreshTokenRepository.deleteRefreshTokenByTokenIdAndOwnerEmail(tokenId, email);
        blackListRepository.setBlackList(accessToken, email);
    }

    public Member updateUsername(String email, String username) {
        Member member = findMemberByEmail(email);
        member.updateUsername(username);
        return member;
    }

    public void updatePassword(String email, String newPassword, String oldPassword) {
        checkPasswordStrength(newPassword);

        Member member = findMemberByEmail(email);
        isSocialLogin(member, "소셜 로그인 서비스를 사용 중인 계정으로 비밀번호가 존재하지 않습니다.");
        checkPassword(oldPassword, member);
        member.updatePassword(passwordEncoder.encode(newPassword));
    }

    public void deleteMember(String email, String password, String accessToken) {
        Member member = findMemberByEmail(email);

        checkPassword(password, member);

        refreshTokenRepository.deleteAllRefreshTokenByEmail(email);
        memberRepository.deleteById(member.getId());
        blackListRepository.setBlackList(accessToken, email);
    }

    public void changePasswordByEmail(String email, String code, String newPassword) {
        checkPasswordStrength(newPassword);
        verificationService.verifyEmail(email, code);

        Member member = findMemberByEmail(email);
        isSocialLogin(member, "소셜 로그인 서비스를 사용 중인 계정으로 비밀번호가 존재하지 않습니다.");
        member.updatePassword(passwordEncoder.encode(newPassword));
    }

    public Map<String, String> getUserInfo(String email) {
        Member member = findMemberByEmail(email);

        HashMap<String, String> map = new HashMap<>();
        map.put("username", member.getUsername());
        map.put("email", member.getEmail());
        map.put("profileImageUrl", member.getProfileImageUri());
        return map;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> {
            log.error("계정이 존재하지 않음");
            return new IllegalArgumentException("계정이 존재하지 않습니다.");
        });
    }

    private void checkRefreshTokenExists(String tokenId, String ownerEmail) {
        if (!refreshTokenRepository.existsByTokenIdAndOwnerEmail(tokenId, ownerEmail)) {
            throw new IllegalArgumentException("존재하지 않는 Refresh 토큰입니다.");
        }
    }

    private void checkPassword(String password, Member member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            log.info("일치하지 않는 비밀번호");
            throw new BadCredentialsException("기존 비밀번호 확인에 실패했습니다.");
        }
    }

    private void checkPasswordStrength(String password) {
        if (PASSWORD_PATTERN.matcher(password).matches()) {
            return;
        }

        log.info("비밀번호 정책 미달");
        throw new IllegalArgumentException("비밀번호는 최소 8자리에 영어, 숫자, 특수문자를 포함해야 합니다.");
    }

    private void isSocialLogin(Member member, String message) {
        if (member.getLoginType() != LoginType.EMAIL_PW) {
            log.info("소셜 로그인 유저의 잘못된 요청");
            throw new UsernameNotFoundException(message);
        }
    }
}
