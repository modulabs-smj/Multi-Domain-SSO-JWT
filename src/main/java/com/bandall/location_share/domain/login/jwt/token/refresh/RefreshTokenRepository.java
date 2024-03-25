package com.bandall.location_share.domain.login.jwt.token.refresh;

import com.bandall.location_share.aop.LoggerAOP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

@LoggerAOP
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    boolean existsByTokenIdAndOwnerEmail(String tokenId, String ownerEmail);

    List<RefreshToken> findAllByOwnerEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query("delete from RefreshToken r where r.expireTime < :expireTime")
    void deleteExpiredTokens(@Param("expireTime") Date expireTime);

    @Modifying(clearAutomatically = true)
    @Query("delete from RefreshToken r where r.tokenId=:tokenId and r.ownerEmail=:ownerEmail")
    void deleteRefreshTokenByTokenIdAndOwnerEmail(@Param("tokenId") String tokenId, @Param("ownerEmail") String ownerEmail);

    @Modifying(clearAutomatically = true)
    @Query("delete from RefreshToken r where r.ownerEmail=:email")
    void deleteAllRefreshTokenByEmail(@Param("email") String email);

    @Query(value = "select r from RefreshToken r",
                countQuery = "select count(r.id) from RefreshToken r")
    Page<RefreshToken> findRefreshTokenWithPaging(Pageable pageable);


}
