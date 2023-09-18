package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.login.LoginService;
import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.login.verification_code.EmailVerificationService;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.web.controller.dto.*;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final EmailVerificationService verificationService;

    @PostMapping("/api/account/create")
    public ApiResponseJson createAccount(@Valid @RequestBody MemberCreateDto memberCreateDto, BindingResult bindingResult) {
        log.info("Creating Account={}", memberCreateDto);
        if(bindingResult.hasErrors()) {
            log.info("Wrong Data");
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        Member member = loginService.createMember(memberCreateDto);
        log.info("Created New Account=>{}", member);
        return new ApiResponseJson(HttpStatus.OK, Map.of(
                "loginType", member.getLoginType(),
                "email", member.getEmail(),
                "username", member.getUsername()
        ));
    }

    @PostMapping("/api/account/auth")
    public ApiResponseJson authAccount(@Valid @RequestBody MemberLoginDto memberLoginDto, BindingResult bindingResult) {
        log.info("Authenticating Account=>{}", memberLoginDto.getEmail());
        if(bindingResult.hasErrors()) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        TokenInfoDto tokenInfoDto = loginService.loginMember(memberLoginDto.getEmail(), memberLoginDto.getPassword());

        log.info("Issued Token Info=>{}", tokenInfoDto);
        return new ApiResponseJson(HttpStatus.OK, tokenInfoDto);
    }

    @PostMapping("/api/account/refresh")
    public ApiResponseJson refreshToken(@Valid @RequestBody TokenInfoDto tokenInfoDto, BindingResult bindingResult) {
        log.info("Refreshing Token={}", tokenInfoDto);
        if(bindingResult.hasErrors()) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        TokenInfoDto newTokenInfo = loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken());
        log.info("Refreshed Token Info=>{}", newTokenInfo);
        return new ApiResponseJson(HttpStatus.OK, newTokenInfo);
    }

    @PostMapping("/api/account/logout")
    public ApiResponseJson logout(@RequestHeader("Authorization") String accessToken, @RequestBody Map<String, String> json,
                                  @AuthenticationPrincipal UserPrinciple user
    ) {
        String refreshToken = json.get("refreshToken");
        log.info("Logout request refreshToken={}", refreshToken);
        if(!StringUtils.hasText(refreshToken)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        loginService.logout(user.getEmail(), accessToken.substring(7), refreshToken);
        log.info("client[{}] logout", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, "Logout Success. Bye ~");
    }

    @PostMapping("/api/account/update-username")
    public ApiResponseJson updateUsername(@RequestBody MemberUpdateDto memberUpdateDto, @AuthenticationPrincipal UserPrinciple user) {
        log.info("Updating Username..email=>{} new_username={}", user.getEmail(), memberUpdateDto.getUsername());
        String username = memberUpdateDto.getUsername();
        if(!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        Member updatedMember = loginService.updateUsername(user.getEmail(), username);

        log.info("Updated User Info=>{}", updatedMember.getUsername());
        return new ApiResponseJson(HttpStatus.OK, Map.of(
                "email", updatedMember.getEmail(),
                "username", updatedMember.getUsername()
        ));
    }

    @PostMapping("/api/account/update-password")
    public ApiResponseJson updatePassword(@RequestBody MemberUpdateDto memberUpdateDto, @AuthenticationPrincipal UserPrinciple user) {
        log.info("Updating Password..email=>{}", user.getEmail());
        String newPassword = memberUpdateDto.getNewPassword();
        String oldPassword = memberUpdateDto.getOldPassword();

        if(!StringUtils.hasText(newPassword) || !StringUtils.hasText(oldPassword)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        loginService.updatePassword(user.getEmail(), newPassword, oldPassword);
        log.info("Password Update Success");
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }

    @GetMapping("/api/account/find-password")
    public ApiResponseJson getPasswordResetCode(@RequestParam(required = false) String email) {
        if(!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }
        verificationService.sendVerificationEmail(email);
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }

    @PostMapping("/api/account/find-password")
    public ApiResponseJson resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        loginService.changePasswordByEmail(resetPasswordDto.getEmail(), resetPasswordDto.getCode(), resetPasswordDto.getNewPassword());
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }

    @PostMapping("/api/account/delete")
    public ApiResponseJson deleteAccount(@RequestHeader("Authorization") String accessToken, @RequestBody Map<String, String> json,
                                         @AuthenticationPrincipal UserPrinciple user) {
        String password = json.get("password");
        log.info("Deleting User={}", user.getEmail());
        if(!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        loginService.deleteMember(user.getEmail(), password, accessToken.substring(7));
        log.info("Success Deleting User={}", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }

    @GetMapping("/api/email-verification")
    public ApiResponseJson getVerifyEmail(@RequestParam(required = false) String email) {
        log.info("Sending Verify Code to=<{}>", email);
        if(!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        verificationService.sendVerificationEmail(email);
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }

    // binding result가 없을 경우 MethodArgumentNotValidException를 advice에서 처리
    @PostMapping("/api/email-verification")
    public ApiResponseJson verifyEmail(@Valid @RequestBody EmailVerifyDto emailVerifyDto) {
        verificationService.verifyEmail(emailVerifyDto.getEmail(), emailVerifyDto.getCode());
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }
}
