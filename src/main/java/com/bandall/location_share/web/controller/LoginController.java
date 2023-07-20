package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.dto.*;
import com.bandall.location_share.domain.login.LoginService;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

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
        log.info("refresh={}", tokenInfoDto);
        if(bindingResult.hasErrors()) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        TokenInfoDto newTokenInfo = loginService.refreshToken(tokenInfoDto.getAccessToken(), tokenInfoDto.getRefreshToken());
        log.info("Reissued Token Info=>{}", newTokenInfo);
        return new ApiResponseJson(HttpStatus.OK, newTokenInfo);
    }

    // 아래부터는 access 토큰이 있어야 접근 가능
    @PostMapping("/api/account/logout")
    public ApiResponseJson logout(@RequestHeader("Authorization") String accessToken, Map<String, String> refreshTokenMap,
                                  @AuthenticationPrincipal UserPrinciple user
    ) {
        String refreshToken = refreshTokenMap.get("refreshToken");
        log.info("logout request refreshToken={}", refreshToken);
        if(!StringUtils.hasText(refreshToken)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        loginService.logout(user.getEmail(), accessToken.substring(7), refreshToken);
        log.info("client[{}] logout", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, "Logout Success. Bye ~");
    }

    // Binding Results 사용 X 한쪽 값만 채워져있음
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

    @PostMapping("/api/account/delete")
    public ApiResponseJson deleteAccount(@RequestHeader("Authorization") String accessToken, @RequestBody Map<String, String> passwordMap, @AuthenticationPrincipal UserPrinciple user) {
        String password = passwordMap.get("password");
        log.info("Deleting User={}", user.getEmail());
        if(!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        loginService.deleteMember(user.getEmail(), password, accessToken.substring(7));
        log.info("Success Deleting User={}", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }

    @PostMapping("/api/email-verification")
    public ApiResponseJson verifyEmail(@RequestBody Map<String, String> codeMap) {
        String code = codeMap.get("code");
        if(!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        loginService.verifyEmail(code);
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }
}
