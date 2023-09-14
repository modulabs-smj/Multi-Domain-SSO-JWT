package com.bandall.location_share.domain.login.jwt.token.refresh;

import com.bandall.location_share.aop.LoggerAOP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@LoggerAOP
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findRefreshTokenByValue(String value);

    List<RefreshToken> findAllByOwnerEmail(String email);

    @Query("select r from RefreshToken r where r.expireTime < :now")
    List<RefreshToken> findRefreshTokensByExpireTimeBefore(@Param("now") Date expireTime);

    @Modifying(clearAutomatically = true)
    @Query("delete from RefreshToken r where r.expireTime < now()")
    void deleteExpiredTokens();

    void deleteRefreshTokenByValue(String value);

    @Modifying(clearAutomatically = true)
    @Query("delete from RefreshToken r where r.ownerEmail=:email")
    void deleteAllRefreshTokenByEmail(@Param("email") String email);
}
