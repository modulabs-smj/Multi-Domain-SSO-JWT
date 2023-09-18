package com.bandall.location_share.domain.login.verification_code;

import com.bandall.location_share.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    boolean existsByCode(String code);

    Optional<VerificationCode> findByCode(String verificationCode);
}
