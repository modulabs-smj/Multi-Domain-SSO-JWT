package com.bandall.location_share.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트 작성용 백도어 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Profile("test")
public class TestController {

    @GetMapping("/test/a")
    public String test() {
        return "test";
    }
}
