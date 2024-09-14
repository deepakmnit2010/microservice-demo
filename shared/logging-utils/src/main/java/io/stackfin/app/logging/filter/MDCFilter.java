package io.stackfin.app.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class MDCFilter extends OncePerRequestFilter {

    private static final String MDC_CLIENT_IP_ADDRESS = "clientIpAddress";
    private static final String MDC_REQUEST_URI = "requestUri";
    private static final String MDC_X_CORRELATION_ID = "correlationId";
    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_X_SESSION_ID = "sessionId";
    private static final String MDC_X_PORT = "port";
    private static final String MDC_X_POD_ID = "podId";
    private static final String MDC_X_EVENT_ID = "eventId";
    private static final String MDC_X_SESSION = "session";
    private static final String MDC_X_DEVICE_ID = "deviceId";
    private static final String MDC_X_DEVICE_IP = "deviceIp";
    private static final String MDC_X_USER_ID = "userId";


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String port = Integer.toString(request.getServerPort());
        MDC.put(MDC_CLIENT_IP_ADDRESS, getIpAddress(request));
        MDC.put(MDC_REQUEST_URI, request.getRequestURI());
        MDC.put(MDC_X_CORRELATION_ID, getCorrelationId(request));
        MDC.put(MDC_TRACE_ID, getTraceId(request));
        MDC.put(MDC_X_SESSION_ID, request.getSession().getId());
        MDC.put(MDC_X_PORT, port);
        MDC.put(MDC_X_POD_ID, getPodId());
        MDC.put(MDC_X_EVENT_ID, getEventId(request));
        MDC.put(MDC_X_SESSION, getSession(request));
        MDC.put(MDC_X_DEVICE_ID, getDeviceId(request));
        MDC.put(MDC_X_DEVICE_IP, getDeviceIp(request));

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CLIENT_IP_ADDRESS);
            MDC.remove(MDC_REQUEST_URI);
            MDC.remove(MDC_X_CORRELATION_ID);
            MDC.remove(MDC_X_SESSION_ID);
            MDC.remove(MDC_X_PORT);
            MDC.remove(MDC_X_POD_ID);
            MDC.remove(MDC_X_EVENT_ID);
            MDC.remove(MDC_X_SESSION);
            MDC.remove(MDC_X_DEVICE_ID);
            MDC.remove(MDC_X_DEVICE_IP);
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_X_USER_ID);
        }
    }

    private String getCorrelationId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Correlation-ID"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress != null
                && !ipAddress.isBlank()
                && ipAddress.split(",").length > 0) {
            String[] ips = ipAddress.split(",");
            return ips[0].trim();
        }
        return Optional.ofNullable(ipAddress)
                .filter(ip -> !ip.isBlank())
                .orElse(request.getRemoteAddr());
    }

    private String getTraceId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Trace-ID"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));
    }

    private String getPodId() {
        return Optional.ofNullable(System.getenv("HOSTNAME"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));
    }

    private String getEventId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-EVENT-TRACE"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));
    }

    private String getSession(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-STACK-FIN-SESSION"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));
    }

    private String getDeviceId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-DEVICE-ID"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));
    }

    private String getDeviceIp(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-DEVICE-IP"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));
    }
}
