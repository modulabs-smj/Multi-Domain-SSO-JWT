package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.login.OAuth2LoginService;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OauthController {

    private final OAuth2LoginService loginService;

    @GetMapping("/oauth/login/kakao")
    public ApiResponseJson getData(@RequestParam(name = "code", required = false) String socialAccessToken) {
        log.info("Kakao access code=>[{}]", socialAccessToken);
        if(!StringUtils.hasText(socialAccessToken)) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }
        TokenInfoDto tokenInfoDto = loginService.socialLogin(socialAccessToken, LoginType.KAKAO);
        return new ApiResponseJson(HttpStatus.OK, tokenInfoDto);
    }

    @PostMapping("/oauth/unlink/kakao")
    public ApiResponseJson deleteAccount(@RequestHeader("Authorization") String accessToken, @RequestParam("code") String socialAccessToken,
                                         @AuthenticationPrincipal UserPrinciple user
    ) {
        loginService.deleteSocialUser(socialAccessToken, accessToken.substring(7), LoginType.KAKAO, user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, "OK");
    }
}
