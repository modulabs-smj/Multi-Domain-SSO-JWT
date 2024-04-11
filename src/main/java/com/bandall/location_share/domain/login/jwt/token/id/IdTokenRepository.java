package com.bandall.location_share.domain.login.jwt.token.id;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdTokenRepository extends JpaRepository<IdToken, Long> {

    boolean existsByTokenId(String tokenId);

    Optional<IdToken> findByTokenId(String tokenId);

    void deleteByTokenId(String tokenId);
}
