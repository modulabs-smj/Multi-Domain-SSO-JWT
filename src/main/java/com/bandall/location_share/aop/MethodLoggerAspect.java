package com.bandall.location_share.aop;

import com.bandall.location_share.aop.trace.MethodLogger;
import com.bandall.location_share.aop.trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class MethodLoggerAspect {
    private final MethodLogger methodLogger;

    public MethodLoggerAspect(MethodLogger methodLogger) {
        this.methodLogger = methodLogger;
    }

    @Pointcut("execution(* com.bandall.location_share.domain.login.LoginService.*(..))")
    public void loginService() {
    }

    @Pointcut("execution(* com.bandall.location_share.web.controller..*(..))")
    public void controller() {
    }

    @Pointcut(
            "within(com.bandall.location_share.domain.login.jwt.token.refresh.RefreshTokenRepository) || " +
                    "within(com.bandall.location_share.domain.member.MemberJpaRepository) || " +
                    "within(com.bandall.location_share.domain.login.jwt.token.access.RedisAccessTokenBlackListRepository)"
    )
    public void repository() {
    }

    @Pointcut("@annotation(LoggerAOP)")
    public void annotation() {
    }

    @Around("loginService() || controller() || repository() || annotation()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceStatus status = null;
        try {
            String message = joinPoint.getSignature().toShortString();
            status = methodLogger.begin(message);

            //target 호출
            Object result = joinPoint.proceed();
            methodLogger.end(status);
            return result;
        } catch (Exception e) {
            methodLogger.exception(status, e);
            throw e;
        }
    }
}
