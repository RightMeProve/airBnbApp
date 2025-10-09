package com.rightmeprove.airbnb.airBnbApp.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

@RestControllerAdvice // Intercepts and modifies all controller responses before they are sent to the client
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply this advice for all controller responses
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // List of routes that should NOT be wrapped (e.g., Swagger docs or health checks)
        List<String> allowedRoutes = List.of("/v3/api-docs", "/actuator");

        // Check if the current request matches any of the allowed routes
        boolean isAllowed = allowedRoutes
                .stream()
                .anyMatch(route -> request.getURI().getPath().contains(route));

        // If already wrapped in ApiResponse or belongs to an allowed route, return as-is
        if (body instanceof ApiResponse<?> || isAllowed) {
            return body;
        }

        // Otherwise, wrap normal responses in a standard ApiResponse for consistency
        return new ApiResponse<>(body);
    }
}
