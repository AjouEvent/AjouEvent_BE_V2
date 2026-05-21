package com.example.ajouevent_be_v2.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 모든 HTTP 요청에 requestId(UUID)를 MDC에 삽입하는 필터.
 * MdcTaskDecorator와 함께 동작하여 비동기 스레드까지 requestId가 전파된다.
 * X-Request-ID 헤더가 안전한 형식이면 재사용, 없거나 유효하지 않으면 신규 생성.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_KEY = "requestId";
    private static final int MAX_REQUEST_ID_LENGTH = 128;
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]+$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
        MDC.put(MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String resolveRequestId(String requestId) {
        if (isValidRequestId(requestId)) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private boolean isValidRequestId(String requestId) {
        return requestId != null
                && !requestId.isBlank()
                && requestId.length() <= MAX_REQUEST_ID_LENGTH
                && REQUEST_ID_PATTERN.matcher(requestId).matches();
    }
}
