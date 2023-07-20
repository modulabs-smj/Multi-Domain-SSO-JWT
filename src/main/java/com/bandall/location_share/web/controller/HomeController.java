package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.member.UserPrinciple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserPrinciple user) {
        return "Login User Info: " + user.toString();
    }
}
