package com.bandall.location_share.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class OauthController {

    @GetMapping("/oauth/login/kakao")
    public String getData(@RequestParam String code) {
        log.info("code=>{}", code);

        return "OK";
    }
}
