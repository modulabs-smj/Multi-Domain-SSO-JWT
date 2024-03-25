package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.login.LoginService;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final LoginService loginService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserPrinciple user) {
        return "Login User Info: " + user.toString();
    }

    @GetMapping("/api/whoami")
    public ApiResponseJson test(@AuthenticationPrincipal UserPrinciple user) {
        log.info("UserPrinciple {}", user);
        Map<String, String> userInfo = loginService.getUserInfo(user.getEmail());
        return new ApiResponseJson(HttpStatus.OK, userInfo);
    }
}
