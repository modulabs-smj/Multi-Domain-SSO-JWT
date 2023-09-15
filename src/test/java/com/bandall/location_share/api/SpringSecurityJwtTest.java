package com.bandall.location_share.api;

import com.bandall.location_share.domain.login.LoginService;
import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.login.jwt.token.RedisAccessTokenBlackListRepository;
import com.bandall.location_share.domain.login.verification_code.VerificationCodeRepository;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberJpaRepository;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.bandall.location_share.web.controller.dto.MemberCreateDto;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.Code;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@Transactional
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

    @PostConstruct
    void postConstruct() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = (HttpStatus) response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR;
            }
        });
    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table member").executeUpdate();
        em.createNativeQuery("truncate table refresh_token").executeUpdate();
        em.createNativeQuery("truncate table verification_code").executeUpdate();
    }

    @Test
    @DisplayName("컨트롤러 접근 테스트")
    void accessTokenTest() throws JsonProcessingException, ParseException {
        // given
        String email = "abasdfasdfc@gma123il.com";
        String password = "bandallgom123!!";
        String username = "반달77";
        addMember(email, password, username);

        Map<String, String> json = new HashMap<>();
        json.put("email", email);
        json.put("password", password);
        json.put("loginType", LoginType.EMAIL_PW.toString());

        // when
        String url = "http://localhost:"+ port+ "/api/account/auth";
        ResponseEntity<String> response = restTemplate.postForEntity(url, json, String.class);
        TokenInfoDto tokenInfoDto = parseTokenInfoDto(response);
        log.info("{}", tokenInfoDto);

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity request = new HttpEntity(httpHeaders);

        httpHeaders.set("Authorization", "Bearer: " + tokenInfoDto.getAccessToken());
        String url2 = "http://localhost:"+ port+ "/api/whoami";
        ResponseEntity<String> response2 = restTemplate.exchange(url2, HttpMethod.GET, request, String.class);

        // then
        log.info("{}", response2.getBody());
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private TokenInfoDto parseTokenInfoDto(ResponseEntity<String> response) throws JsonProcessingException {
        Map<String, Object> responseJson = mapper.readValue(response.getBody(), Map.class);
        Map<String, String> tokenInfoMap = (Map<String, String>) responseJson.get("data");
        TokenInfoDto tokenInfoDto = TokenInfoDto.builder()
                .accessToken(tokenInfoMap.get("accessToken"))
                .accessTokenExpireTime(Date.from(Instant.parse(tokenInfoMap.get("accessTokenExpireTime"))))
                .refreshToken(tokenInfoMap.get("refreshToken"))
                .refreshTokenExpireTime(Date.from(Instant.parse(tokenInfoMap.get("refreshTokenExpireTime"))))
                .ownerEmail(tokenInfoMap.get("ownerEmail"))
                .tokenId(tokenInfoMap.get("tokenId"))
                .build();
        return tokenInfoDto;
    }

    @Transactional
    public void addMember(String email, String password, String username) {
        Map<String, String> json = new HashMap<>();
        json.put("email", email);
        json.put("password", password);
        json.put("username", username);

        String url = "http://localhost:"+ port+ "/api/account/create";
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, json, String.class);
    }
}
