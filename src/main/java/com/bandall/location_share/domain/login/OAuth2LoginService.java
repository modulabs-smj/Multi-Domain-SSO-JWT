package com.bandall.location_share.domain.login;

import com.bandall.location_share.domain.exceptions.EmailNotVerifiedException;
import com.bandall.location_share.domain.login.jwt.dto.AccessRefreshTokenDto;
import com.bandall.location_share.domain.login.jwt.token.TokenProvider;
import com.bandall.location_share.domain.login.jwt.token.access.RedisAccessTokenBlackListRepository;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshToken;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshTokenRepository;
import com.bandall.location_share.domain.login.oauth2.OAuth2UserInfoProvider;
import com.bandall.location_share.domain.login.oauth2.userinfo.OAuth2UserInfo;
import com.bandall.location_share.domain.login.verification_code.EmailVerificationService;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberRepository;
import com.bandall.location_share.domain.member.enums.LoginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OAuth2LoginService {
    private static final String EMAIL_REGEX_PATTERN = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";
    private final MemberRepository memberRepository;
    private final OAuth2UserInfoProvider oAuth2UserInfoProvider;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisAccessTokenBlackListRepository blackListRepository;
    private final EmailVerificationService verificationService;

    public AccessRefreshTokenDto socialLogin(String accessToken, LoginType loginType) {
        OAuth2UserInfo profile = oAuth2UserInfoProvider.getProfile(accessToken, loginType);

        String email = profile.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("소셜 로그인을 사용하기 위해선 이메일 제공 동의를 해주셔야 합니다.");
        }

        if (!isEmailValid(email)) {
            throw new IllegalArgumentException("잘못된 이메일입니다.");
        }

        boolean exists = memberRepository.existsByEmail(email);
        if (!exists) {
            Member newMember = Member.builder()
                    .email(email)
                    .password("")
                    .loginType(LoginType.SOCIAL_KAKAO)
                    .username(profile.getName())
                    .build();
            
            newMember.updateProfileImageUri(profile.getProfileImageUri());
            newMember.updateEmailVerified(profile.getIsEmailVerified());
            memberRepository.save(newMember);
        }

        Member member = findMemberByEmail(email);

        if (member.getLoginType() == LoginType.EMAIL_PW) {
            log.info("잘못된 로그인 타입");
            throw new IllegalArgumentException("이 계정은 소셜 로그인을 사용하지 않습니다.");
        }

        if (!member.isEmailVerified()) {
            verificationService.sendVerificationEmail(member.getEmail());
            throw new EmailNotVerifiedException("이메일 인증이 되어 있지 않습니다. [" + email + "]로 보낸 메일을 통해 인증을 진행해 주세요.", email);
        }

        AccessRefreshTokenDto accessRefreshTokenDto = tokenProvider.createAccessRefreshTokenPair(member);
        RefreshToken newRefreshToken = RefreshToken.builder()
                .ownerEmail(accessRefreshTokenDto.getOwnerEmail())
                .tokenId(accessRefreshTokenDto.getTokenId())
                .expireTime(accessRefreshTokenDto.getRefreshTokenExpireTime())
                .build();
        refreshTokenRepository.save(newRefreshToken);
        return accessRefreshTokenDto;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("계정이 존재하지 않습니다."));
    }

    public void deleteSocialUser(String socialAccessToken, String tokenId, LoginType loginType, String email) {
        OAuth2UserInfo profile = oAuth2UserInfoProvider.getProfile(socialAccessToken, loginType);

        String oAuthEmail = profile.getEmail();
        if (!email.equals(oAuthEmail)) {
            log.info("일치하지 않는 계정");
            throw new IllegalArgumentException("일치하지 않는 계정입니다.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("계정이 존재하지 않습니다."));

        oAuth2UserInfoProvider.unlinkSocial(socialAccessToken, loginType);
        memberRepository.deleteById(member.getId());
        memberRepository.flush();
        refreshTokenRepository.deleteAllRefreshTokenByEmail(email);
        blackListRepository.setBlackList(tokenId, email);
    }

    private boolean isEmailValid(String email) {
        return Pattern.matches(EMAIL_REGEX_PATTERN, email);
    }
}
