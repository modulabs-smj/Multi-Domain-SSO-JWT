package com.bandall.location_share.aop;

import com.bandall.location_share.aop.trace.MethodLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {
    @Bean
    public MethodLoggerAspect methodLoggerAspect() {
        return new MethodLoggerAspect(methodLogger());
    }

    public MethodLogger methodLogger() {
        return new MethodLogger();
    }
}
