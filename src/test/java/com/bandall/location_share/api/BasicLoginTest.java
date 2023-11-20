package com.bandall.location_share.api;

import com.bandall.location_share.domain.exceptions.EmailNotVerifiedException;
import com.bandall.location_share.domain.login.LoginService;
import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.login.jwt.token.access.RedisAccessTokenBlackListRepository;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshTokenRepository;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberJpaRepository;
import com.bandall.location_share.web.controller.dto.MemberCreateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Rollback
@Transactional
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class BasicLoginTest {

    @Autowired
    private MemberJpaRepository memberRepository;

    @Autowired
    private RedisAccessTokenBlackListRepository blackListRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private LoginService loginService;

    @Autowired
    private EntityManager em;

    @AfterEach
    void afterEach() {
        em.createNativeQuery("delete from verification_code").executeUpdate();
        em.createNativeQuery("delete from refresh_token").executeUpdate();
        em.createNativeQuery("delete from member").executeUpdate();
    }

    @Test
    @DisplayName("회원가입 테스트")
    @Transactional
    void 회원가입() throws JsonProcessingException {
        // given
        String email = "abc@gmail.com";
        String password = "12345aa6!";
        String username = "bandallgom";
        MemberCreateDto memberCreateDto = new MemberCreateDto(email, password, username);

        // when
        loginService.createMember(memberCreateDto);

        // then
        Member member = memberRepository.findByEmail(email).get();
        assertThat(member.getUsername()).isEqualTo(username);
    }

    //    @Test
    @DisplayName("로그인 테스트[이메일 인증 전]")
    void 로그인1() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username, false);

        // when

        // then
        assertThatThrownBy(() -> loginService.loginMember(email, password)).isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    @DisplayName("로그인 테스트[이메일 인증 후]")
    void 로그인2() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username, true);

        // when
        TokenInfoDto tokenInfoDto = loginService.loginMember(email, password);
        log.info("{}", tokenInfoDto);

        // then
        assertThat(tokenInfoDto.getOwnerEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("로그인 테스트[로그인 실패]")
    void 로그인3() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username, true);

        // then
        assertThatThrownBy(() -> loginService.loginMember(email + "1234", password)).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> loginService.loginMember(email, password + "1234")).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("유저 이름 변경")
    void 유저이름변경() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        String newUsername = "반달124";
        addMember(email, password, username, true);

        // when
        loginService.updateUsername(email, newUsername);

        // then
        Member member = memberRepository.findByEmail(email).get();
        assertThat(member.getUsername()).isEqualTo(newUsername);
    }

    @Test
    @DisplayName("비밀번호 재설정")
    void 비밀번호재설정() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        String newPassword = "jsm12124!";
        addMember(email, password, username, true);

        // when
        loginService.updatePassword(email, newPassword, password);

        // then
        loginService.loginMember(email, newPassword);
    }

    @Test
    @DisplayName("비밀번호 재설정 실패")
    void 비밀번호재설정실패() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        String newPassword = "jsm1234!";
        addMember(email, password, username, true);

        // then
        assertThatThrownBy(() -> loginService.updatePassword(email, newPassword, password + "123")).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> loginService.updatePassword(email, "1234", password)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> loginService.updatePassword("123@asd.com", newPassword, password)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Access token 재발급")
    void 토큰재발급() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username, true);

        // when
        TokenInfoDto tokenInfoDto = loginService.loginMember(email, password);

        // then
        TokenInfoDto tokenInfoDtoNew = loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken());
        String blackListEmail = (String) blackListRepository.getBlackList(tokenInfoDto.getAccessToken());
        // 블랙리스트 등록
        assertThat(blackListEmail).isEqualTo(email);
        // 새 토큰 발급
        assertThat(tokenInfoDto.getTokenId()).isNotEqualTo(tokenInfoDtoNew.getTokenId());
    }

    @Test
    @DisplayName("Access token 재발급 실패")
    void 토큰재발급실패() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username, true);

        // when
        TokenInfoDto tokenInfoDto = loginService.loginMember(email, password);
        TokenInfoDto tokenInfoDto2 = loginService.loginMember(email, password);

        // then
        assertThatThrownBy(() -> loginService.refreshToken("", tokenInfoDto.getRefreshToken()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> loginService.refreshToken(tokenInfoDto.getAccessToken(), ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> loginService.refreshToken("", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto2.getRefreshToken()))
                .isInstanceOf(IllegalArgumentException.class); // 토큰 교차 검증
        assertThatThrownBy(() -> {
            loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken());
            loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken());
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("RefreshToken 만료")
    void 토큰재발급실패2() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username, true);

        // when
        TokenInfoDto tokenInfoDto = loginService.loginMember(email, password);
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // then
        assertThatThrownBy(() -> loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("토큰 갱신 2회 시도")
    void 토큰재발급실패3() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username, true);

        // when
        TokenInfoDto tokenInfoDto = loginService.loginMember(email, password);
        loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken());

        // then
        assertThatThrownBy(() -> loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("계정 삭제")
    void 계정삭제() {
        // given
        String email = "abc@gmail.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username, true);

        // when
        TokenInfoDto tokenInfoDto = loginService.loginMember(email, password);
        loginService.deleteMember(email, password, tokenInfoDto.getAccessToken());

        // then
        String blackListEmail = (String) blackListRepository.getBlackList(tokenInfoDto.getAccessToken());
        assertThat(blackListEmail).isEqualTo(email); // 블랙리스트 등록
        assertThat(memberRepository.findByEmail(email)).isNotPresent();
        assertThat(refreshTokenRepository.findAllByOwnerEmail(email).size()).isEqualTo(0);
    }

    private void addMember(String email, String password, String username, boolean setEmailVerified) {
        MemberCreateDto memberCreateDto = new MemberCreateDto(email, password, username);
        loginService.createMember(memberCreateDto);

        if (setEmailVerified) setEmailVerified(email);
    }

    private void setEmailVerified(String email) {
        Member member = memberRepository.findByEmail(email).get();
        member.updateEmailVerified(true);
    }
}
