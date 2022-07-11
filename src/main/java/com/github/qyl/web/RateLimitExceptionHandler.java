package com.github.qyl.web;

import com.github.qyl.RateLimiterProperties;
import com.github.qyl.exception.RateLimitException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ConditionalOnProperty(prefix = RateLimiterProperties.PREFIX, name = "exceptionHandler.enable", havingValue = "true", matchIfMissing = true)
public class RateLimitExceptionHandler {

    private final RateLimiterProperties limiterProperties;

    public RateLimitExceptionHandler(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

    @ExceptionHandler(value = RateLimitException.class)
    @ResponseBody
    public ResponseEntity exceptionHandler(RateLimitException e) {

        if (e.getMap() != null) {
            return ResponseEntity.status(limiterProperties.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getMap());
        }

        return ResponseEntity.status(limiterProperties.getStatusCode())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfter()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(limiterProperties.getResponseBody());
    }
}
