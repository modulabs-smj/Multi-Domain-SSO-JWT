package com.bandall.location_share.domain.member;

import com.bandall.location_share.aop.LoggerAOP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@LoggerAOP
public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select m from Member m left join fetch m.verificationCode v where m.email = :email")
    Optional<Member> findByEmailJoinVerifyCode(@Param("email") String email);
}
