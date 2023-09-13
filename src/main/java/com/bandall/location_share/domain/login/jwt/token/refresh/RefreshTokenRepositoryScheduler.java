package com.bandall.location_share.domain.login.jwt.token.refresh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenRepositoryScheduler {
    private final RefreshTokenRepository repository;

    // @SpringBootTest에서는 @Transcational 붙일 경우 롤백되기 때문에 db에 쿼리가 날아가지 않는다.
    // 1시간마다 삭제
    @Transactional
    @Scheduled(fixedDelay = 3600000)
    public void deleteExpired() {
        log.info("[Scheduler] [{}] Deleting expired refresh tokens ", new Timestamp(System.currentTimeMillis()));
        repository.deleteExpiredTokens();
    }
}
