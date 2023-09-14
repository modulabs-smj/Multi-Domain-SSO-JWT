package com.bandall.location_share.api;

import com.bandall.location_share.domain.login.LoginService;
import com.bandall.location_share.domain.login.jwt.token.RedisAccessTokenBlackListRepository;
import com.bandall.location_share.domain.login.verification_code.VerificationCodeRepository;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberJpaRepository;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.Code;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringSecurityJwtTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Autowired
    private RedisAccessTokenBlackListRepository blackListRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EntityManager em;

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table member").executeUpdate();
        em.createNativeQuery("truncate table refresh_token").executeUpdate();
        em.createNativeQuery("truncate table verification_code").executeUpdate();
    }

    @Test
    @DisplayName("컨트롤러 접근 테스트")
    @Transactional
    void accessTokenTest() throws JsonProcessingException {
        // given
        Map<String, String> json = new HashMap<>();
        json.put("email", "abc@gmail.com");
        json.put("password", "bandallgom123!!");
        json.put("username", "반달7");

        // when
        String url = "http://localhost:"+ port+ "/api/account/create";
        ResponseEntity<String> response = restTemplate.postForEntity(url, json, String.class);
        ApiResponseJson responseJson = mapper.readValue(response.getBody(), ApiResponseJson.class);
        log.info("responseJson={}", responseJson);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Member findMember = memberRepository.findByEmail("abc@gmail.com").get();
        assertThat(findMember.getUsername()).isEqualTo("반달7");
    }

    private void addMember(String email, String password, String username) {
        Map<String, String> json = new HashMap<>();
        json.put("email", email);
        json.put("password", password);
        json.put("username", username);

        String url = "http://localhost:"+ port+ "/api/account/create";
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, json, String.class);
    }

    @Transactional
    public void setEmailVerified(String email) {
        em.createQuery("update Member m set m.isEmailVerified = true where m.email = :email")
                .setParameter("email", email)
                .executeUpdate();
    }
}
