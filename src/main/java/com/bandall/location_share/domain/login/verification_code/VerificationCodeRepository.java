package com.bandall.location_share.domain.login.verification_code;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    boolean existsByVerificationCode(String code);

    void deleteByEmail(String email);

    Optional<VerificationCode> findByVerificationCode(String verificationCode);
}
