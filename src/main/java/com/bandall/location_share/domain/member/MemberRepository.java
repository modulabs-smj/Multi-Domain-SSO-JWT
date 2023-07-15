package com.bandall.location_share.domain.member;

import com.bandall.location_share.domain.dto.MemberUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 리펙토링 이후 사용하지 않는 함수
 */
@RequiredArgsConstructor
public class MemberRepository {
    //직접 사용하는거로 수정?
    private final MemberJpaRepository repository;

    public Member save(Member member) {
        return repository.save(member);
    }

    public Optional<Member> updatePassword(String email, String password) {
        Member foundMember = repository.findByEmail(email).orElseThrow();
        foundMember.updatePassword(password);
        return Optional.of(foundMember);
    }

    public Optional<Member> updateUsername(String email, String username) {
        Member foundMember = repository.findByEmail(email).orElseThrow();
        foundMember.updateUsername(username);
        return Optional.of(foundMember);
    }

    public void delete(Long memberId) {
        repository.deleteById(memberId);
    }

    public List<Member> findAll() {
        return repository.findAll();
    }

    public Optional<Member> findById(Long memberId) {
        return repository.findById(memberId);
    }

    public Optional<Member> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<Member> findByUsername(String username) {
        return repository.findByUsername(username);
    }

}
