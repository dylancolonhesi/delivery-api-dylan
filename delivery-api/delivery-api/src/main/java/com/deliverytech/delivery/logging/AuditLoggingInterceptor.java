package com.deliverytech.delivery.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditLoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AuditLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.info("AUDIT REQUEST: method={}, uri={}, correlationId={}", request.getMethod(), request.getRequestURI(), MDC.get("correlationId"));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex != null) {
            logger.error("AUDIT ERROR: uri={}, status={}, correlationId={}, error={}", request.getRequestURI(), response.getStatus(), MDC.get("correlationId"), ex.getMessage(), ex);
        } else {
            logger.info("AUDIT RESPONSE: uri={}, status={}, correlationId={}", request.getRequestURI(), response.getStatus(), MDC.get("correlationId"));
        }
    }
}
