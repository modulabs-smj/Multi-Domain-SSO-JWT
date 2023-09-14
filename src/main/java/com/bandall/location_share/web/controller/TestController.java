package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * 테스트 작성용 백도어 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Profile("test")
public class TestController {

    private final MemberJpaRepository memberRepository;

    @Transactional
    @PostMapping("/test/emailVerified")
    public String setEmailVerified(@RequestBody Map<String, String> json) {
        String email = json.get("email");

        Member member = memberRepository.findByEmail(email).get();
        member.updateEmailVerified(true);
        return "OK";
    }

    @GetMapping("/test/a")
    public String test() {
        return "test";
    }
}
