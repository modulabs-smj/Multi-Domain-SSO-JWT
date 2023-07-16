package com.bandall.location_share.domain.member;

import com.bandall.location_share.aop.LoggerAOP;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@LoggerAOP
public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsername(String username);

    boolean existsByEmail(String email);
}
