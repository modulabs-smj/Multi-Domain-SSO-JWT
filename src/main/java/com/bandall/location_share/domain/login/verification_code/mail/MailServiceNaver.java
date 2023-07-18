package com.bandall.location_share.domain.login.verification_code.mail;

import com.bandall.location_share.web.filter.LogFilter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceNaver implements MailService{

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${smtp.username}")
    String from;

    @Override
    public void sendEmailWithTemplate(String title, String email, String templateName, HashMap<String, String> values, String traceId) {
        MDC.put(LogFilter.TRACE_ID, traceId);
        long startTime = System.currentTimeMillis();
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setSubject(title);
            helper.setTo(email);

            Context context = new Context();
            values.forEach((key, value) -> {
                context.setVariable(key, value);
            });

            String html = templateEngine.process(templateName, context);
            helper.setText(html, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("메일 전송 실패 title=[{}] email=[{}] template=[{}] map=[{}] totalTime=[{}]", title, email, templateName, values, totalTime, e);
        }
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("title=[{}] email=[{}] totalTime=[{}] 이메일 전송 성공", title, email, totalTime);
        MDC.clear();
    }
}
