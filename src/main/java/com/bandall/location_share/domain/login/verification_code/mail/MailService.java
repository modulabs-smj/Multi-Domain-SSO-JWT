package com.bandall.location_share.domain.login.verification_code.mail;


import org.springframework.scheduling.annotation.Async;
import java.util.HashMap;

public interface MailService {
    @Async
    void sendEmailWithTemplate(String title, String to, String templateName, HashMap<String, String> values, String traceId);
}
