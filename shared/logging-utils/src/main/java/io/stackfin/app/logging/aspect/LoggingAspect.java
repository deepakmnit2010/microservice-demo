package io.stackfin.app.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;

@Component
@Aspect
@Slf4j(topic = "au-logging")
@RequiredArgsConstructor
public class LoggingAspect {

    ObjectMapper objectMapper;

    @Autowired
    public LoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("(execution(* io.stackfin.app.*.controller.*.*(..)) && @annotation(postMapping))")
    public Object auditPostEndpoints(ProceedingJoinPoint proceedingJoinPoint, PostMapping postMapping)
            throws Throwable {
        long start = System.currentTimeMillis();
        Object[] requestArgs = proceedingJoinPoint.getArgs();
        String methodName = proceedingJoinPoint.getSignature().toShortString();
        String requestUri = MDC.get("requestUri");
        log.info("Request for endpoint {} and controller method {} is {}",
                requestUri,
                methodName,
                Arrays.asList(requestArgs));
        String responseJson = null;
        try {
            var response = proceedingJoinPoint.proceed();
            responseJson = objectMapper.writeValueAsString(response);
            return response;
        } catch (Exception e) {
            responseJson = e.getMessage();
            log.error("Exception while executing the method {}. Exception message is {}",
                    methodName, responseJson, e);
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            log.info("Response for endpoint {} and controller method {} is {}, timeTaken {} ms",
                    requestUri,
                    methodName,
                    responseJson, executionTime);
        }
    }
}
