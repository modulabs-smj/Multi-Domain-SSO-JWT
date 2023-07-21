package com.bandall.location_share.domain.login.jwt.token;

import com.bandall.location_share.aop.LoggerAOP;
import com.bandall.location_share.domain.dto.TokenInfoDto;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshToken;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberDetails;
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

    protected static final String KEY_AUTHORITIES = "auth";
    protected static final String KEY_TOKEN_ID = "tokenId";
    protected static final String KEY_USERNAME = "username";

    protected final String secrete;
    protected final long accessTokenValidationInMilliseconds;
    protected final long refreshTokenValidationInMilliseconds;
    protected Key hashKey;

    protected RedisAccessTokenBlackListRepository blackListRepository;

    public TokenProvider(String secrete, long accessTokenValidationInSeconds, long refreshTokenValidationInMilliseconds, RedisAccessTokenBlackListRepository blackListRepository) {
        this.secrete = secrete;
        this.accessTokenValidationInMilliseconds = accessTokenValidationInSeconds * 1000;
        this.refreshTokenValidationInMilliseconds = refreshTokenValidationInMilliseconds * 1000;
        byte[] keyBytes = Decoders.BASE64.decode(secrete);
        this.hashKey = Keys.hmacShaKeyFor(keyBytes);
        this.blackListRepository = blackListRepository;
    }

    // 토큰 생성 함수
    public TokenInfoDto createTokenWithAuthentication(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long currentTime = (new Date()).getTime();
        Date accessTokenExpireTime = new Date(currentTime + this.accessTokenValidationInMilliseconds);
        Date refreshTokenExpireTime = new Date(currentTime + this.refreshTokenValidationInMilliseconds);
        String email = ((MemberDetails) authentication.getPrincipal()).getEmail();
        String tokenId = UUID.randomUUID().toString();

        // Access 토큰
        String accessToken = Jwts.builder()
                .setSubject(email)
                .claim(KEY_AUTHORITIES, authorities)
                .claim(KEY_USERNAME, authentication.getName())
                .claim(KEY_TOKEN_ID, tokenId)
                .signWith(hashKey, SignatureAlgorithm.HS512)
                .setExpiration(accessTokenExpireTime)
                .compact();

        // Refresh 토큰
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpireTime)
                .setSubject(email)
                .claim(KEY_TOKEN_ID, tokenId)
                .signWith(hashKey, SignatureAlgorithm.HS512)
                .compact();

        return TokenInfoDto.builder()
                .ownerEmail(email)
                .tokenId(tokenId)
                .accessToken(accessToken)
                .accessTokenExpireTime(accessTokenExpireTime)
                .refreshToken(refreshToken)
                .refreshTokenExpireTime(refreshTokenExpireTime)
                .build();
    }

    // refresh 토큰으로 access 토큰 갱신 시 사용
    public TokenInfoDto createTokenWithMember(Member member) {
        long currentTime = (new Date()).getTime();
        Date accessTokenExpireTime = new Date(currentTime + this.accessTokenValidationInMilliseconds);
        Date refreshTokenExpireTime = new Date(currentTime + this.refreshTokenValidationInMilliseconds);
        String tokenId = UUID.randomUUID().toString();

        // Access 토큰
        String accessToken = Jwts.builder()
                .setSubject(member.getEmail())
                .claim(KEY_AUTHORITIES, member.getRole())
                .claim(KEY_USERNAME, member.getUsername())
                .claim(KEY_TOKEN_ID, tokenId)
                .signWith(hashKey, SignatureAlgorithm.HS512)
                .setExpiration(accessTokenExpireTime)
                .compact();

        // Refresh 토큰
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpireTime)
                .setSubject(member.getEmail())
                .claim(KEY_TOKEN_ID, tokenId)
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
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(hashKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(KEY_AUTHORITIES).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 커스텀한 UserPrinciple 객체 사용 -> 이후 추가적인 데이터를 토큰에 넣을 경우 UserPrinciple 객체 및 이 클래스의 함수들 수정 필요
        UserPrinciple principle = new UserPrinciple(claims.getSubject(), claims.get(KEY_USERNAME, String.class), authorities);

        return new UsernamePasswordAuthenticationToken(principle, token, authorities);
    }

//    // 이후에는 claim을 두 번 하지 않도록 새로운 dto를 만들어서 전달할 수 있게 수정
//    // 최종적으로 이 함수 삭제
//    public RefreshToken getRefreshTokenData(String token) {
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(hashKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//
//        return new RefreshToken(claims.getSubject(), token, claims.getExpiration());
//    }

    // 토큰 유효성 검사 -> access, refresh 토큰 둘 다 검증하는 함수이므로 access 토큰 블랙리스트는 체크하지 않는다.
    public TokenValidationResult validateToken(String token) {
        TokenValidationResult validResult = new TokenValidationResult(false, TokenType.ACCESS, null,null, null);
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder().setSigningKey(hashKey).build().parseClaimsJws(token).getBody();
            if(claims.get(KEY_AUTHORITIES) == null) validResult.setTokenType(TokenType.REFRESH);

            validResult.setResult(true);
            validResult.setTokenId(claims.get(KEY_TOKEN_ID, String.class));
            validResult.setTokenStatus(TokenStatus.TOKEN_VALID);
            return validResult;
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 tokenId를 부여할 수 있게 수정
            log.info("만료된 jwt 토큰");
            claims = e.getClaims();
            validResult.setTokenStatus(TokenStatus.TOKEN_EXPIRED);
            validResult.setTokenId(claims.get(KEY_TOKEN_ID, String.class));
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 jwt 서명");
            validResult.setTokenStatus(TokenStatus.TOKEN_WRONG_SIGNATURE);
        }  catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 jwt 서명");
            validResult.setTokenStatus(TokenStatus.TOKEN_HASH_NOT_SUPPORTED);
        } catch (IllegalArgumentException e) {
            log.info("잘못된 jwt 토큰");
            validResult.setTokenStatus(TokenStatus.TOKEN_WRONG_SIGNATURE);
        } catch (Exception e) {
            log.error("Redis 에러");
            validResult.setTokenStatus(TokenStatus.TOKEN_VALIDATION_TRY_FAILED);
            validResult.setException(e);
        }
        return validResult;
    }

    // access 토큰과 refresh 토큰 검증, 차후에 TokenValidationResult에 tokenID를 넣어 검증하는 로직 추가 가능
    public TokenValidationResult isAccessTokenAndRefreshTokenValid(String accessToken, String refreshToken) {
        TokenValidationResult refTokenRes = validateToken(refreshToken);
        TokenValidationResult aTokenRes = validateToken(accessToken);
        TokenValidationResult totalResult = new TokenValidationResult(true, null, null, TokenStatus.TOKEN_VALID, null);

        if(refTokenRes.getTokenStatus() == TokenStatus.TOKEN_EXPIRED) {
            log.info("Expired Refresh Token");
            totalResult.setTokenStatus(TokenStatus.TOKEN_EXPIRED);
            return totalResult;
        }

        if(!refTokenRes.getResult()) {
            log.info("Wrong Refresh Token");
            totalResult.setTokenStatus(TokenStatus.TOKEN_WRONG_SIGNATURE);
            return totalResult;
        }

        // 잘못된 access 토큰일 경우에만 => 재발급 할꺼라 만료된 것도 OK
        if(!aTokenRes.getResult() && aTokenRes.getTokenStatus() != TokenStatus.TOKEN_EXPIRED) {
            log.info("Wrong Access Token");
            totalResult.setTokenStatus(TokenStatus.TOKEN_WRONG_SIGNATURE);
            return totalResult;
        }

//        // claim으로 tokenID를 비교하는 로직으로 대체하려 했으나 redis가 속도가 더 빨라서 취소
//        // => claim을 추가적으로 안할 수 있게 코드 개선
//        if(isAccessTokenBlackList(accessToken)) {
//            log.info("Discarded Token");
//            totalResult.setTokenStatus(TokenStatus.TOKEN_IS_BLACKLIST);
//            return totalResult;
//        }
        log.info("{} {}", refTokenRes.getTokenId(), aTokenRes.getTokenId());
        if(!refTokenRes.getTokenId().equals(aTokenRes.getTokenId())) {
            log.info("Wrong refresh & access token pair");
            totalResult.setTokenStatus(TokenStatus.TOKEN_ID_NOT_MATCH);
            return totalResult;
        }

        totalResult.setTokenId(aTokenRes.getTokenId());
        return totalResult;
    }

    public boolean isAccessTokenBlackList(String accessToken) {
        if(blackListRepository.isKeyBlackList(accessToken)) {
            log.info("폐기된 jwt 토큰");
            return true;
        } else {
            return false;
        }
    }
}
