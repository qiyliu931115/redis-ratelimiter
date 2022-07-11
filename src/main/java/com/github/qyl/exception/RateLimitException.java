package com.github.qyl.exception;

import java.util.Map;

public class RateLimitException extends RuntimeException{

    private long retryAfter;

    private Map<String, String> map;

    public RateLimitException(String message, long retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public RateLimitException(String message, Map<String, String> map) {
        super(message);
        this.map = map;
    }
    public long getRetryAfter() {
        return retryAfter;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
