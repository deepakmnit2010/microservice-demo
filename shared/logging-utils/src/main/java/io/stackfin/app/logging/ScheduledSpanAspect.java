package io.stackfin.app.logging;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledSpanAspect {

    private final Tracer tracer;

    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void annotatedWithScheduled() {
    }

    @Pointcut("@annotation(org.jobrunr.jobs.annotations.Job)")
    public void annotatedWithJobScheduled() {
    }

    @Around("annotatedWithJobScheduled() || annotatedWithScheduled()")
    public Object wrapScheduledIntoSpan(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();
        var span = tracer.nextSpan().name(methodName).start();
        MDC.put("traceId", UUID.randomUUID().toString());
        try (var ignoredSpanInScope = tracer.withSpan(span.start())) {
            return pjp.proceed();
        } finally {
            span.end();
        }
    }
}
