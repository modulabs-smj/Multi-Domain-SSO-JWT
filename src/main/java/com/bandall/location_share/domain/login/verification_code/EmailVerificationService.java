package com.bandall.location_share.domain.login.verification_code;

import com.bandall.location_share.domain.login.verification_code.mail.MailService;
import com.bandall.location_share.domain.member.Member;
import com.bandall.location_share.domain.member.MemberJpaRepository;
import com.bandall.location_share.web.filter.LogFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
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
    private final MemberJpaRepository memberRepository;

    public void sendVerificationEmail(String email) {
        String code = "";
        long currentTime = (new Date()).getTime();
        Date accessTokenExpireTime = new Date(currentTime + verificationCodeExpireTime * 1000);

        VerificationCode verificationCode = null;
        // code 중복 방지
        while(true) {
            code = UUID.randomUUID().toString().substring(0, 7).toUpperCase();
            verificationCode = VerificationCode.builder()
                    .email(email)
                    .verificationCode(code)
                    .expireTime(accessTokenExpireTime)
                    .build();
            if(!verificationCodeRepository.existsByVerificationCode(code)) break;
        }
        log.info("이메일 인증 요청 전송 email:{}, code:{}", email, code);
        verificationCodeRepository.deleteByEmail(email);
        verificationCodeRepository.flush();
        verificationCodeRepository.save(verificationCode);

        HashMap<String, String> map = new HashMap<>();
        map.put("verificationCode", code);
        map.put("expireTime", "만료 시간은 " + verificationCodeExpireTime/3600 + "시간입니다.");

        mailService.sendEmailWithTemplate("이메일 인증", email, "mail-verification-inline", map, MDC.get(LogFilter.TRACE_ID));
    }

    public void verifyEmail(String code) {
        log.info("이메일 인증 시도 code:{}", code);
        Optional<VerificationCode> byVerificationCode = verificationCodeRepository.findByVerificationCode(code);
        if(byVerificationCode.isEmpty()) {
            log.info("잘못된 인증 코드 code:{}", code);
            throw new IllegalStateException("잘못된 인증 코드입니다.");
        }
        VerificationCode verificationCode = byVerificationCode.get();

        if((new Date()).compareTo(verificationCode.getExpireTime()) != -1) {
            log.info("이메일 인증 코드 인증 시간 만료");
            throw new IllegalStateException("인증 시간이 만료 되었습니다.");
        }

        String email = verificationCode.getEmail();

        Member member = memberRepository.findByEmail(email).orElseThrow(() ->
                new IllegalStateException("현재 존재하지 않는 계정입니다."));
        member.updateEmailVerified(true);
        verificationCodeRepository.deleteByEmail(email);
        verificationCodeRepository.flush();
        log.info("이메일 인증 성공");
    }
}
