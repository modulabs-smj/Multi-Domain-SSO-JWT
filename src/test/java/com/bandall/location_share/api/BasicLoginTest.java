package com.bandall.location_share.api;

import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberJpaRepository;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.Code;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Rollback
@Transactional
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BasicLoginTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MemberJpaRepository memberRepository;

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

//    @PostConstruct
//    void postConstruct() {
//        restTemplate.
//    }

    @Test
    void test() {
        String url = "http://localhost:"+ port+ "/test/a";
        ResponseEntity<String> response = restTemplate.postForEntity(url, "", String.class);
    }

    @Test
    @DisplayName("회원가입 테스트")
    @Transactional
    void 회원가입() throws JsonProcessingException {
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

    @Test
    @DisplayName("로그인 테스트[이메일 인증 전]")
    void 로그인1() throws JsonProcessingException {
        // given
        addMember("abc@gmail.com", "bandallgom123!!", "반달77");
        Map<String, String> json = new HashMap<>();
        json.put("loginType", "EMAIL_PW");
        json.put("email", "abc@gmail.com");
        json.put("password", "bandallgom123!!");


        // when
        String url = "http://localhost:"+ port + "/api/account/auth";

        // then
        assertThatThrownBy(() -> restTemplate.postForEntity(url, json, String.class)).isInstanceOf(ResourceAccessException.class);
    }

    /**
     * update 쿼리를 날려도 서버에 반영이 안됨
     *
     */
    @Test
    @DisplayName("로그인 테스트[이메일 인증 생략]")
    void 로그인2() throws JsonProcessingException {
        // given
        addMember("abc@gmail.com", "bandallgom123!!", "반달77");

        Map<String, String> json = new HashMap<>();
        json.put("loginType", "EMAIL_PW");
        json.put("email", "abc@gmail.com");
        json.put("password", "bandallgom123!!");

        // when
        String url = "http://localhost:"+ port + "/api/account/auth";
        ResponseEntity<String> response = restTemplate.postForEntity(url, json, String.class);
        ApiResponseJson responseJson = mapper.readValue(response.getBody(), ApiResponseJson.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseJson.getCode()).isEqualTo(Code.NO_ERROR);
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
