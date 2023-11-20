package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.login.LoginService;
import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.login.verification_code.EmailVerificationService;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.web.controller.advice.ControllerMessage;
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

    private static final String LOGIN_TYPE_KEY = "loginType";
    private static final String EMAIL_KEY = "email";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String REFRESH_TOKEN_KEY = "refreshToken";
    private static final String AUTH_HEADER_KEY = "Authorization";
    private final LoginService loginService;
    private final EmailVerificationService verificationService;

    @PostMapping("/api/account/create")
    public ApiResponseJson createNewAccount(@Valid @RequestBody MemberCreateDto memberCreateDto, BindingResult bindingResult) {
        log.info("Request received to create account with data: {}", memberCreateDto);
        if (bindingResult.hasErrors()) {
            log.error("Data provided for account creation is invalid: {}", bindingResult);
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        Member member = loginService.createMember(memberCreateDto);
        log.info("Account successfully created with details: {}", member);

        return new ApiResponseJson(HttpStatus.OK, Map.of(
                LOGIN_TYPE_KEY, member.getLoginType(),
                EMAIL_KEY, member.getEmail(),
                USERNAME_KEY, member.getUsername()
        ));
    }

    @PostMapping("/api/account/auth")
    public ApiResponseJson authenticateAccountAndIssueToken(@Valid @RequestBody MemberLoginDto memberLoginDto, BindingResult bindingResult) {
        log.info("Received request to authenticate account: {}", memberLoginDto.getEmail());
        if (bindingResult.hasErrors()) {
            log.error("Invalid account authentication request with errors: {}", bindingResult.getAllErrors());
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        TokenInfoDto tokenInfoDto = loginService.loginMember(memberLoginDto.getEmail(), memberLoginDto.getPassword());

        log.info("Token issued for account: {}", tokenInfoDto.getTokenId());
        return new ApiResponseJson(HttpStatus.OK, tokenInfoDto);
    }

    @PostMapping("/api/account/refresh")
    public ApiResponseJson refreshTokens(@Valid @RequestBody TokenInfoDto tokenInfoDto, BindingResult bindingResult) {
        log.info("Received request to refresh token: {}", tokenInfoDto);
        if (bindingResult.hasErrors()) {
            log.error("Invalid token refresh request with errors: {}", bindingResult.getAllErrors());
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        TokenInfoDto newTokenInfo = loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken());
        log.info("Token refreshed: {}", newTokenInfo.getTokenId());
        return new ApiResponseJson(HttpStatus.OK, newTokenInfo);
    }

    @PostMapping("/api/account/logout")
    public ApiResponseJson logout(@RequestHeader(AUTH_HEADER_KEY) String authHeader, @RequestBody Map<String, String> json,
                                  @AuthenticationPrincipal UserPrinciple user) {
        log.info("Received request to logout account: {}", user.getEmail());
        String refreshToken = json.get(REFRESH_TOKEN_KEY);

        if (!StringUtils.hasText(refreshToken)) {
            log.error("Invalid logout request: refreshToken is missing");
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        loginService.logout(user.getEmail(), authHeader.substring(7), refreshToken);
        log.info("Account logged out: {}", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.LOGOUT_MSG);
    }

    @PostMapping("/api/account/update-username")
    public ApiResponseJson updateUsername(@RequestBody MemberUpdateDto memberUpdateDto, @AuthenticationPrincipal UserPrinciple user) {
        log.info("Received request to update username for account: {}", user.getEmail());
        String username = memberUpdateDto.getUsername();
        if (!StringUtils.hasText(username)) {
            log.error("Invalid username update request: new username is missing");
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        Member updatedMember = loginService.updateUsername(user.getEmail(), username);
        log.info("Username updated for account: {}", updatedMember.getUsername());

        return new ApiResponseJson(HttpStatus.OK, Map.of(
                EMAIL_KEY, updatedMember.getEmail(),
                USERNAME_KEY, updatedMember.getUsername()
        ));
    }

    @PostMapping("/api/account/update-password")
    public ApiResponseJson updatePassword(@RequestBody MemberUpdateDto memberUpdateDto, @AuthenticationPrincipal UserPrinciple user) {
        log.info("Received request to update password for account: {}", user.getEmail());
        String newPassword = memberUpdateDto.getNewPassword();
        String oldPassword = memberUpdateDto.getOldPassword();

        if (!StringUtils.hasText(newPassword) || !StringUtils.hasText(oldPassword)) {
            log.error("Invalid password update request: newPassword or oldPassword is missing");
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        loginService.updatePassword(user.getEmail(), newPassword, oldPassword);
        log.info("Password updated for account: {}", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.OK_MSG);
    }

    @GetMapping("/api/account/find-password")
    public ApiResponseJson getPasswordResetCode(@RequestParam(required = false) String email) {
        log.info("Received request to get password reset code for account: {}", email);
        if (!StringUtils.hasText(email)) {
            log.error("Invalid password reset code request: email is missing");
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        verificationService.sendVerificationEmail(email);

        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.OK_MSG);
    }

    @PostMapping("/api/account/find-password")
    public ApiResponseJson resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        log.info("Received request to reset password for account: {}", resetPasswordDto.getEmail());
        loginService.changePasswordByEmail(resetPasswordDto.getEmail(), resetPasswordDto.getCode(), resetPasswordDto.getNewPassword());

        log.info("Password reset for account: {}", resetPasswordDto.getEmail());
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.OK_MSG);
    }

    @PostMapping("/api/account/delete")
    public ApiResponseJson deleteAccount(@RequestHeader(AUTH_HEADER_KEY) String accessToken,
                                         @RequestBody Map<String, String> json, @AuthenticationPrincipal UserPrinciple user) {
        log.info("Received request to delete account: {}", user.getEmail());
        String password = json.get(PASSWORD_KEY);
        if (!StringUtils.hasText(password)) {
            log.error("Invalid account deletion request: password is missing");
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        loginService.deleteMember(user.getEmail(), password, accessToken.substring(7));
        log.info("Account deleted: {}", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.OK_MSG);
    }

    @GetMapping("/api/email-verification")
    public ApiResponseJson getVerifyEmail(@RequestParam(required = false) String email) {
        log.info("Received request to get email verification for account: {}", email);
        if (!StringUtils.hasText(email)) {
            log.error("Invalid email verification request: email is missing");
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        verificationService.sendVerificationEmail(email);
        log.info("Email verification sent for account: {}", email);
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.OK_MSG);
    }

    // binding result가 없을 경우 MethodArgumentNotValidException를 advice에서 처리
    @PostMapping("/api/email-verification")
    public ApiResponseJson verifyEmail(@Valid @RequestBody EmailVerifyDto emailVerifyDto) {
        log.info("Received request to verify email for account: {}", emailVerifyDto.getEmail());
        verificationService.verifyEmail(emailVerifyDto.getEmail(), emailVerifyDto.getCode());

        log.info("Received request to verify email for account: {}", emailVerifyDto.getEmail());
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.OK_MSG);
    }
}
