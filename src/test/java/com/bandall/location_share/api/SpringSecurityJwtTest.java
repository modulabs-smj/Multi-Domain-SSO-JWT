package com.bandall.location_share.api;

import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.member.enums.LoginType;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.TokenStatusCode;
import com.bandall.location_share.web.filter.LogFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private ObjectMapper mapper;

    String email = "abasdfasdfc@gma123il.com";
    String password = "bandallgom123!!";
    String username = "반달77";

    @PostConstruct
    void postConstruct() {
        MDC.put(LogFilter.TRACE_ID, "TEST");
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = (HttpStatus) response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR;
            }
        });
        addMember(email, password, username);
    }

    @Test
    @DisplayName("컨트롤러 접근 테스트[성공]")
    void accessTokenTest() throws JsonProcessingException {
        // given
        TokenInfoDto tokenInfoDto = getTokenInfoDto(email, password);

        // when
        HttpEntity<String> httpEntity = setHttpEntity(tokenInfoDto);
        String url = makeUrl("/api/whoami");
        ResponseEntity<ApiResponseJson> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ApiResponseJson.class);

        // then
        log.info("{}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo(TokenStatusCode.OK);
    }

    @Test
    @DisplayName("컨트롤러 접근 테스트[실패]")
    void accessTokenTestFail() throws JsonProcessingException {
        // given
        TokenInfoDto tokenInfoDto = getTokenInfoDto(email, password);
        tokenInfoDto.setAccessToken("fake token value");

        // when
        HttpEntity<String> httpEntity = setHttpEntity(tokenInfoDto);
        String url = makeUrl("/api/whoami");
        ResponseEntity<ApiResponseJson> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ApiResponseJson.class);

        // then
        log.info("{}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isNotEqualTo(TokenStatusCode.OK);
    }

    @Test
    @DisplayName("Auth 헤더 X")
    void accessTokenTestFail2() throws JsonProcessingException {
        // given
        TokenInfoDto tokenInfoDto = getTokenInfoDto(email, password);
        tokenInfoDto.setAccessToken("fake token value");

        // when
        HttpEntity<String> httpEntity = new HttpEntity<>("");
        String url = makeUrl("/api/whoami");
        ResponseEntity<ApiResponseJson> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ApiResponseJson.class);

        // then
        log.info("{}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo(TokenStatusCode.NO_AUTH_HEADER);
    }

    @Test
    @DisplayName("잘못된 Auth 헤더")
    void accessTokenTestFail3() throws JsonProcessingException {
        // given
        TokenInfoDto tokenInfoDto = getTokenInfoDto(email, password);
        tokenInfoDto.setAccessToken(" " + tokenInfoDto.getAccessToken());

        // when
        HttpEntity<String> httpEntity = new HttpEntity<>("");
        String url = makeUrl("/api/whoami");
        ResponseEntity<ApiResponseJson> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ApiResponseJson.class);

        // then
        log.info("{}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo(TokenStatusCode.NO_AUTH_HEADER);
    }

    @Test
    @DisplayName("Access Token 시간 초과")
    void accessTokenTimeout() throws JsonProcessingException {
        // given
        TokenInfoDto tokenInfoDto = getTokenInfoDto(email, password);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // when
        HttpEntity<String> httpEntity = setHttpEntity(tokenInfoDto);
        String url = makeUrl("/api/whoami");
        ResponseEntity<ApiResponseJson> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ApiResponseJson.class);
        log.info("{}", response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo(TokenStatusCode.TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws JsonProcessingException {
        // given
        TokenInfoDto tokenInfoDto = getTokenInfoDto(email, password);

        // when
        HttpEntity<String> httpEntity = setHttpEntity(tokenInfoDto, Map.of("refreshToken", tokenInfoDto.getRefreshToken()));
        log.info("{}", httpEntity);
        String logoutUrl = makeUrl("/api/account/logout");
        restTemplate.exchange(logoutUrl, HttpMethod.POST, httpEntity, ApiResponseJson.class);

        String accessUrl = makeUrl("/api/whoami");
        HttpEntity<String> httpEntity2 = setHttpEntity(tokenInfoDto);
        ResponseEntity<ApiResponseJson> response = restTemplate.exchange(accessUrl, HttpMethod.GET, httpEntity2, ApiResponseJson.class);
        log.info("{}", response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo(TokenStatusCode.TOKEN_IS_BLACKLIST);
    }

    @Test
    @DisplayName("토큰 재발급 후 기존 토큰으로 접속 시도")
    void accessTokenAfterRefresh() throws JsonProcessingException {
        // given
        addMember(email, password, username);
        TokenInfoDto tokenInfoDto = getTokenInfoDto(email, password);

        // when
        String refreshUrl = makeUrl("/api/account/refresh");
        ResponseEntity<ApiResponseJson> response1 = restTemplate.postForEntity(refreshUrl, Map.of(
                "refreshToken", tokenInfoDto.getRefreshToken(),
                "accessToken", tokenInfoDto.getAccessToken()), ApiResponseJson.class);
        log.info("{}", response1);

        String accessUrl = makeUrl("/api/whoami");
        HttpEntity<String> httpEntity = setHttpEntity(tokenInfoDto);
        ResponseEntity<ApiResponseJson> response = restTemplate.exchange(accessUrl, HttpMethod.GET, httpEntity, ApiResponseJson.class);
        log.info("{}", response);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo(TokenStatusCode.TOKEN_IS_BLACKLIST);
    }

    private String makeUrl(String url) {
        return "http://localhost:" + port + url;
    }

    private HttpEntity setHttpEntity(TokenInfoDto tokenInfoDto, Map<String, Object> json) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + tokenInfoDto.getAccessToken());
        return new HttpEntity<>(json, httpHeaders);
    }

    private HttpEntity<String> setHttpEntity(TokenInfoDto tokenInfoDto) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + tokenInfoDto.getAccessToken());
        return new HttpEntity<>(httpHeaders);
    }

    private TokenInfoDto getTokenInfoDto(String email, String password) throws JsonProcessingException {
        Map<String, String> json = new HashMap<>();
        json.put("email", email);
        json.put("password", password);
        json.put("loginType", LoginType.EMAIL_PW.toString());

        String url = "http://localhost:"+ port+ "/api/account/auth";
        ResponseEntity<String> response = restTemplate.postForEntity(url, json, String.class);
        TokenInfoDto tokenInfoDto = parseTokenInfoDto(response);
        return tokenInfoDto;
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

    public void addMember(String email, String password, String username) {
        Map<String, String> json = new HashMap<>();
        json.put("email", email);
        json.put("password", password);
        json.put("username", username);

        String url = "http://localhost:"+ port+ "/api/account/create";
        restTemplate.postForEntity(url, json, String.class);
    }
}
