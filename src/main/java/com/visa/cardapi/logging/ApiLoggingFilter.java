package com.visa.cardapi.logging;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {
    private final ApiLogService service;

    public ApiLoggingFilter(ApiLogService service) {
        this.service = service;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(req);
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(res);
        chain.doFilter(request, response);
        String requestBody = "";
        if (request.getContentAsByteArray().length > 0) {
            requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
        }
        String responseBody = "";
        if (response.getContentAsByteArray().length > 0) {
            responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        }
        ApiLog log = ApiLog.builder()
                .endpoint(request.getRequestURI())
                .httpMethod(request.getMethod())
                .requestPayload(requestBody)
                .responsePayload(responseBody)
                .statusCode(response.getStatus())
                .ipAddress(req.getRemoteAddr())
                .build();

        service.save(log);
        response.copyBodyToResponse();
    }
}