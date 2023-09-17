package com.bandall.location_share.domain.login.verification_code;

import com.bandall.location_share.domain.login.verification_code.mail.MailService;
import com.bandall.location_share.web.filter.LogFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class EmailVerificationService {

    @Value("${verification.email.expire-time}")
    private Long verificationCodeExpireTime;

    private final MailService mailService;
    private final VerificationCodeRepository verificationCodeRepository;

    public void sendVerificationEmail(String email) {
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        LocalDateTime accessTokenExpireTime = LocalDateTime.now().plusSeconds(verificationCodeExpireTime);

        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .verificationCode(code)
                .expireTime(accessTokenExpireTime)
                .build();

        log.info("이메일 인증 요청 전송 <EMAIL:{}, CODE:{}>", email, code);
        verificationCodeRepository.deleteByEmail(email);
        verificationCodeRepository.save(verificationCode);

        HashMap<String, String> map = new HashMap<>();
        map.put("verificationCode", code);
        map.put("expireTime", "만료 시간은 " + verificationCodeExpireTime/3600 + "시간입니다.");

        // thymeleaf를 통해 이메일 전송
        mailService.sendEmailWithTemplate("이메일 인증", email, "mail-verification-inline", map, MDC.get(LogFilter.TRACE_ID));
    }

    public void verifyEmail(String email, String code) {
        log.info("이메일 인증 시도 <EMAIL:{}, CODE:{}>", email, code);

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email).orElseThrow(() -> {
            log.info("존재하지 않는 인증코드 <EMAIL:{}>", email);
            return new IllegalStateException("잘못된 인증 정보입니다.");
        });

        if(!verificationCode.getVerificationCode().equals(code)) {
            log.info("잘못된 인증 코드 {}:{}", verificationCode.getVerificationCode(), code);
            throw new IllegalStateException("잘못된 인증 정보입니다.");
        }

        if(LocalDateTime.now().isAfter(verificationCode.getExpireTime())) {
            log.info("이메일 인증 코드 인증 시간 만료");
            throw new IllegalStateException("인증 시간이 만료 되었습니다.");
        }

        verificationCodeRepository.deleteByEmail(email);
        log.info("이메일 인증 성공");
    }
}
