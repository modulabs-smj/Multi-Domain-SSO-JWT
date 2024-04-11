package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.exceptions.IdTokenNotValidException;
import com.bandall.location_share.domain.login.LoginService;
import com.bandall.location_share.domain.login.jwt.dto.AccessRefreshTokenDto;
import com.bandall.location_share.domain.login.jwt.dto.IdTokenDto;
import com.bandall.location_share.domain.login.verification_code.EmailVerificationService;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.web.controller.advice.ControllerMessage;
import com.bandall.location_share.web.controller.dto.*;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
public class LoginControllerV2 {

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
            log.info("Data provided for account creation is invalid: {}", bindingResult);
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

    @PostMapping("/api/account/login")
    public ApiResponseJson verifyAccountAndIssueIdToken(@Valid @RequestBody MemberLoginDto memberLoginDto, BindingResult bindingResult,
                                         HttpServletResponse response) {
        log.info("Received request to verify account: {}", memberLoginDto.getEmail());
        if (bindingResult.hasErrors()) {
            log.info("Invalid account verification request with errors: {}", bindingResult.getAllErrors());
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        IdTokenDto idTokenDto = loginService.issueIdToken(memberLoginDto.getEmail(), memberLoginDto.getPassword());
        log.info("Account verified: {}", idTokenDto);

        Cookie idTokenCookie = new Cookie("idToken", idTokenDto.getIdToken());
        idTokenCookie.setHttpOnly(true);
//        idTokenCookie.setSecure(true);
        idTokenCookie.setPath("/api/account");
        idTokenCookie.setMaxAge(idTokenCookie.getMaxAge());
        response.addCookie(idTokenCookie);

        return new ApiResponseJson(HttpStatus.OK, idTokenDto);
    }

    @PostMapping("/api/account/auth")
    public ApiResponseJson authenticateAccountAndIssueToken(@CookieValue(value = "idToken", required = false, defaultValue = "") String idToken) {
        if(!StringUtils.hasText(idToken)) {
            log.info("Invalid token authentication request: idToken is missing");
            throw new IdTokenNotValidException("IdToken is missing");
        }
        AccessRefreshTokenDto accessRefreshTokenDto = loginService.issueAccessRefreshToken(idToken);

        log.info("Token issued for account: {}", accessRefreshTokenDto.getTokenId());
        return new ApiResponseJson(HttpStatus.OK, accessRefreshTokenDto);
    }

    @PostMapping("/api/account/refresh")
    public ApiResponseJson refreshTokens(@Valid @RequestBody AccessRefreshTokenDto accessRefreshTokenDto, BindingResult bindingResult) {
        log.info("Received request to refresh token");
        if (bindingResult.hasErrors()) {
            log.info("Invalid token refresh request with errors: {}", bindingResult.getAllErrors());
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        AccessRefreshTokenDto newTokenInfo = loginService.refreshToken(accessRefreshTokenDto.getAccessToken(), accessRefreshTokenDto.getRefreshToken());
        log.info("Token refreshed: {}", newTokenInfo.getTokenId());
        return new ApiResponseJson(HttpStatus.OK, newTokenInfo);
    }

    @PostMapping("/api/account/logout")
    public ApiResponseJson logout(@CookieValue(value = "idToken", required = false, defaultValue = "") String idToken,
                                  HttpServletResponse response) {
        if(!StringUtils.hasText(idToken)) {
            log.info("Invalid token authentication request: idToken is missing");
            throw new IdTokenNotValidException("IdToken is missing");
        }
        loginService.logout(idToken);
        Cookie idTokenCookie = new Cookie("idToken", null);
        idTokenCookie.setMaxAge(0);
        response.addCookie(idTokenCookie);
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.LOGOUT_MSG);
    }

    @PostMapping("/api/account/delete")
    public ApiResponseJson deleteAccount(@RequestHeader(AUTH_HEADER_KEY) String accessToken,
                                         @RequestBody Map<String, String> json, @AuthenticationPrincipal UserPrinciple user) {
        log.info("Received request to delete account: {}", user.getEmail());
        String password = json.get(PASSWORD_KEY);
        if (!StringUtils.hasText(password)) {
            log.info("Invalid account deletion request: password is missing");
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        loginService.deleteMember(user.getEmail(), password);
        log.info("Account deleted: {}", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.OK_MSG);
    }
}
