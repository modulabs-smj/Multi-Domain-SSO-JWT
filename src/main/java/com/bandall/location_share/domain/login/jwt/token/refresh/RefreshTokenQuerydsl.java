package com.bandall.location_share.domain.login.jwt.token.refresh;

import com.bandall.location_share.domain.admin.dto.QSessionCountDto;
import com.bandall.location_share.domain.admin.dto.SessionCountDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefreshTokenQuerydsl {

    private final JPAQueryFactory queryFactory;

    public Map<String, Long> getDuplicatedSessionCount(Set<String> emails) {
        QRefreshToken refreshToken = QRefreshToken.refreshToken;
        List<SessionCountDto> sessionCountDtos = queryFactory
                .select(new QSessionCountDto(
                        refreshToken.ownerEmail,
                        refreshToken.id.count()))
                .from(refreshToken)
                .where(refreshToken.ownerEmail.in(emails))
                .groupBy(refreshToken.ownerEmail)
                .fetch();

        Map<String, Long> sessionCounts = new HashMap<>();
        for (SessionCountDto dto: sessionCountDtos) {
            sessionCounts.put(dto.getEmail(), dto.getSessionCounts());
        }

        return sessionCounts;
    }

    public RefreshToken getRefreshTokenByTokenId(String tokenId) {
        QRefreshToken refreshToken = QRefreshToken.refreshToken;
        return queryFactory.select(refreshToken)
                .from(refreshToken)
                .where(refreshToken.tokenId.eq(tokenId))
                .fetchOne();
    }

    public long deleteRefreshTokenInList(List<String> tokenIds) {
        QRefreshToken refreshToken = QRefreshToken.refreshToken;
        return queryFactory.delete(refreshToken)
                .where(refreshToken.tokenId.in(tokenIds))
                .execute();
    }
}