package com.bandall.location_share.domain.login.jwt.token;

import com.bandall.location_share.aop.LoggerAOP;
import com.bandall.location_share.domain.login.jwt.dto.TokenInfoDto;
import com.bandall.location_share.domain.login.jwt.dto.TokenValidationResult;
import com.bandall.location_share.domain.login.jwt.token.access.RedisAccessTokenBlackListRepository;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.UserPrinciple;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

//토큰 생성 및 검증 클래스 수동 빈 등록 사용
@Slf4j
@LoggerAOP
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String TOKEN_ID_KEY = "tokenId";
    private static final String USERNAME_KEY = "username";

    private static final String TOKEN_ID = "tokenId";

    private final Key hashKey;
    private final long accessTokenValidationInMilliseconds;
    private final long refreshTokenValidationInMilliseconds;

    private final RedisAccessTokenBlackListRepository blackListRepository;

    public TokenProvider(String secrete, long accessTokenValidationInSeconds, long refreshTokenValidationSeconds, RedisAccessTokenBlackListRepository blackListRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secrete);
        this.hashKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidationInMilliseconds = accessTokenValidationInSeconds * 1000;
        this.refreshTokenValidationInMilliseconds = refreshTokenValidationSeconds * 1000;
        this.blackListRepository = blackListRepository;
    }

    public TokenInfoDto createToken(Member member) {
        long currentTime = (new Date()).getTime();
        Date accessTokenExpireTime = new Date(currentTime + this.accessTokenValidationInMilliseconds);
        Date refreshTokenExpireTime = new Date(currentTime + this.refreshTokenValidationInMilliseconds);
        String tokenId = UUID.randomUUID().toString(); // TODO: 고유한 tokenID 발급하는 로직 구성

        // Access 토큰
        String accessToken = Jwts.builder()
                .setSubject(member.getEmail())
                .claim(AUTHORITIES_KEY, member.getRoles())
                .claim(USERNAME_KEY, member.getUsername())
                .claim(TOKEN_ID_KEY, tokenId)
                .signWith(hashKey, SignatureAlgorithm.HS512)
                .setExpiration(accessTokenExpireTime)
                .compact();

        // Refresh 토큰
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpireTime)
                .setSubject(member.getEmail())
                .claim(TOKEN_ID_KEY, tokenId)
                .signWith(hashKey, SignatureAlgorithm.HS512)
                .compact();

        return TokenInfoDto.builder()
                .ownerEmail(member.getEmail())
                .tokenId(tokenId)
                .accessToken(accessToken)
                .accessTokenExpireTime(accessTokenExpireTime)
                .refreshToken(refreshToken)
                .refreshTokenExpireTime(refreshTokenExpireTime)
                .build();
    }

    // access 토큰을 인자로 전달받아 클레임을 만들어 권한 정보 반환
    public Authentication getAuthentication(String token, Claims claims) {
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 커스텀한 UserPrinciple 객체 사용 -> 이후 추가적인 데이터를 토큰에 넣을 경우 UserPrinciple 객체 및 이 클래스의 함수들 수정 필요
        UserPrinciple principle = new UserPrinciple(claims.getSubject(), claims.get(USERNAME_KEY, String.class),
                claims.get(TOKEN_ID, String.class), authorities);

        return new UsernamePasswordAuthenticationToken(principle, token, authorities);
    }

    // 토큰 유효성 검사 -> access, refresh 토큰 둘 다 검증하는 함수이므로 access 토큰 블랙리스트는 체크하지 않는다.
    public TokenValidationResult validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(hashKey).build().parseClaimsJws(token).getBody();
            TokenType tokenType = claims.get(AUTHORITIES_KEY) == null ? TokenType.REFRESH : TokenType.ACCESS;
            return new TokenValidationResult(TokenStatus.TOKEN_VALID, tokenType, claims.get(TOKEN_ID_KEY, String.class), claims);
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰");
            return getExpiredTokenValidationResult(e);
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명");
            return new TokenValidationResult(TokenStatus.TOKEN_WRONG_SIGNATURE, null, null, null);
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 서명");
            return new TokenValidationResult(TokenStatus.TOKEN_HASH_NOT_SUPPORTED, null, null, null);
        } catch (IllegalArgumentException e) {
            log.info("잘못된 JWT 토큰");
            return new TokenValidationResult(TokenStatus.TOKEN_WRONG_SIGNATURE, null, null, null);
        }
    }

    private TokenValidationResult getExpiredTokenValidationResult(ExpiredJwtException e) {
        Claims claims = e.getClaims();
        TokenType tokenType = claims.get(AUTHORITIES_KEY) == null ? TokenType.REFRESH : TokenType.ACCESS;
        return new TokenValidationResult(TokenStatus.TOKEN_EXPIRED, tokenType, claims.get(TOKEN_ID_KEY, String.class), null);
    }

    public TokenValidationResult isAccessAndRefreshTokenValid(String accessToken, String refreshToken) {
        TokenValidationResult refTokenRes = validateToken(refreshToken);
        TokenValidationResult accessTokenRes = validateToken(accessToken);

        if (!isRefreshTokenValid(refTokenRes)) {
            return refTokenRes;
        }

        if (!isAccessTokenValid(accessTokenRes)) {
            return accessTokenRes;
        }

        if (!isTokenPairValid(refTokenRes, accessTokenRes)) {
            return new TokenValidationResult(TokenStatus.TOKEN_ID_NOT_MATCH, null, null, null);
        }

        return new TokenValidationResult(TokenStatus.TOKEN_VALID, null, refTokenRes.getTokenId(), refTokenRes.getClaims());
    }

    private boolean isRefreshTokenValid(TokenValidationResult refTokenRes) {
        if (refTokenRes.getTokenStatus() == TokenStatus.TOKEN_EXPIRED) {
            log.info("Expired Refresh Token");
            refTokenRes.setTokenStatus(TokenStatus.TOKEN_EXPIRED);
            return false;
        }

        if (!refTokenRes.isValid()) {
            log.info("Wrong Refresh Token");
            refTokenRes.setTokenStatus(TokenStatus.TOKEN_WRONG_SIGNATURE);
            return false;
        }

        return true;
    }

    private boolean isAccessTokenValid(TokenValidationResult accessTokenRes) {
        // 잘못된 access 토큰일 경우에만 => 재발급 할꺼라 만료된 것도 OK
        if (!accessTokenRes.isValid() && accessTokenRes.getTokenStatus() != TokenStatus.TOKEN_EXPIRED) {
            log.info("Wrong Access Token");
            accessTokenRes.setTokenStatus(TokenStatus.TOKEN_WRONG_SIGNATURE);
            return false;
        }

        return true;
    }

    private boolean isTokenPairValid(TokenValidationResult refTokenRes, TokenValidationResult aTokenRes) {
        // tokenId 쌍 검증
        if (!refTokenRes.getTokenId().equals(aTokenRes.getTokenId())) {
            log.info("Wrong refresh & access tokenId pair");
            return false;
        }

        return true;
    }

    public boolean isAccessTokenBlackList(String accessToken) {
        if (blackListRepository.isKeyBlackList(accessToken)) {
            log.info("Blacklisted Access Token");
            return true;
        } else {
            return false;
        }
    }
}
