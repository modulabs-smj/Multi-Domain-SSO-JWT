package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.login.OAuth2LoginService;
import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.bandall.location_share.web.controller.advice.ControllerMessage;
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

    private static final String AUTH_HEADER_KEY = "Authorization";
    private static final String SOCIAL_ACCESS_TOKEN_KEY = "code";
    private final OAuth2LoginService loginService;

    @GetMapping("/oauth/login/kakao")
    public ApiResponseJson getData(@RequestParam(name = "code", required = false) String socialAccessToken) {
        log.info("Received Kakao OAuth login request with access code: {}", socialAccessToken);
        if (!StringUtils.hasText(socialAccessToken)) {
            log.error("Kakao OAuth login request failed: access code is missing");
            throw new IllegalArgumentException(ControllerMessage.WRONG_REQUEST_ERROR_MSG);
        }

        TokenInfoDto tokenInfoDto = loginService.socialLogin(socialAccessToken, LoginType.SOCIAL_KAKAO);
        log.info("Kakao OAuth login successful with token info: {}", tokenInfoDto);
        return new ApiResponseJson(HttpStatus.OK, tokenInfoDto);
    }

    @PostMapping("/oauth/unlink/kakao")
    public ApiResponseJson deleteAccount(@RequestHeader(AUTH_HEADER_KEY) String accessToken, @RequestParam(SOCIAL_ACCESS_TOKEN_KEY) String socialAccessToken,
                                         @AuthenticationPrincipal UserPrinciple user
    ) {
        log.info("Received request to unlink Kakao OAuth for account: {}", user.getEmail());
        loginService.deleteSocialUser(socialAccessToken, accessToken.substring(7), LoginType.SOCIAL_KAKAO, user.getEmail());

        log.info("Unlinked Kakao OAuth for account: {}", user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, ControllerMessage.OK_MSG);
    }
}
