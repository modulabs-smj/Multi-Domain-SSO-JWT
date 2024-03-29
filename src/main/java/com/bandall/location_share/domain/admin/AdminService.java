package com.bandall.location_share.domain.admin;

import com.bandall.location_share.domain.admin.dto.MemberInfo;
import com.bandall.location_share.domain.admin.dto.RoleInfo;
import com.bandall.location_share.domain.admin.dto.SessionCountDto;
import com.bandall.location_share.domain.exceptions.NoPageException;
import com.bandall.location_share.domain.login.jwt.token.access.RedisAccessTokenBlackListRepository;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshToken;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshTokenQuerydsl;
import com.bandall.location_share.domain.login.jwt.token.refresh.RefreshTokenRepository;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberRepository;
import com.bandall.location_share.domain.member.enums.Role;
import com.bandall.location_share.web.controller.dto.PageDto;
import com.bandall.location_share.web.controller.dto.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final RefreshTokenQuerydsl refreshTokenQuerydsl;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisAccessTokenBlackListRepository blackListRepository;


    public RoleInfo getRoles(String adminEmail) {
        checkAdminRole(adminEmail);

        return new RoleInfo();
    }

    public void addRole(String adminEmail, String memberEmail, Role role) {
        checkAdminRole(adminEmail);

        if (Objects.equals(memberEmail, adminEmail) && role.equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("본인의 ADMIN 권한을 수정할 수 없습니다.");
        }

        Member member = findMemberByEmail(memberEmail);

        member.addRole(role);

        log.info("Updated member roles={}", member.getRoles());
    }

    public void removeRole(String adminEmail, String memberEmail, Role role) {
        checkAdminRole(adminEmail);

        if (Objects.equals(memberEmail, adminEmail) && role.equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("본인의 ADMIN 권한을 수정할 수 없습니다.");
        }

        Member member = findMemberByEmail(memberEmail);

        member.removeRole(role);

        log.info("Updated member roles={}", member.getRoles());
    }

    public PageDto<MemberInfo> getMemberInfos(String adminEmail, int page, int size) {
        checkAdminRole(adminEmail);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<Member> memberPage = memberRepository.findAllMembersAndCount(pageRequest);

        return new PageDto<>(memberPage.map(MemberInfo::new));
    }

    public PageDto<SessionInfoDto> getSessionInfos(String adminEmail, int page, int size) {
        checkAdminRole(adminEmail);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.Direction.DESC, "id");
        Page<RefreshToken> refreshTokens = refreshTokenRepository.findRefreshTokenWithPaging(pageRequest);

        if(!refreshTokens.hasContent()) {
            throw new NoPageException("페이지가 존재하지 않습니다.");
        }

        Set<String> userEmails = refreshTokens.stream()
                .map(RefreshToken::getOwnerEmail)
                .collect(Collectors.toSet());

        Map<String, Long> sessionCounts = refreshTokenQuerydsl.getDuplicatedSessionCount(userEmails);

        List<SessionInfoDto> sessionInfoDtos = refreshTokens.getContent().stream()
                .map(r -> new SessionInfoDto(r, sessionCounts.get(r.getOwnerEmail())))
                .toList();

        return new PageDto<>(sessionInfoDtos, page, refreshTokens.getTotalPages());
    }

    public void setTokenBlacklist(String adminEmail, String tokenId) {
        checkAdminRole(adminEmail);

        RefreshToken refreshToken = refreshTokenQuerydsl.getRefreshTokenByTokenId(tokenId);
        if(refreshToken == null) {
            throw new IllegalArgumentException("존재하지 않는 Token ID 입니다.");
        }

        refreshTokenQuerydsl.deleteRefreshTokenInList(List.of(refreshToken.getTokenId()));
        blackListRepository.setBlackList(refreshToken.getTokenId(), refreshToken.getOwnerEmail());
    }

    public MemberInfo getMemberInfo(String adminEmail, String memberEmail) {
        checkAdminRole(adminEmail);

        Member member = findMemberByEmail(memberEmail);

        return new MemberInfo(member);
    }

    private void checkAdminRole(String adminEmail) {
        Member admin = findMemberByEmail(adminEmail);

        if (!admin.hasRole(Role.ROLE_ADMIN)) {
            log.warn("NO ADMIN Role: {}", adminEmail);
            throw new IllegalStateException("관리자 권한이 존재하지 않습니다.");
        }
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> {
            log.error("계정이 존재하지 않음");
            return new IllegalArgumentException("계정이 존재하지 않습니다.");
        });
    }
}
