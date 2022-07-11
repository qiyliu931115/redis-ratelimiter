package com.github.qyl.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public final class LuaScript {

    private LuaScript(){}
    private static final String RATE_LIMITER_FILE_PATH = "META-INF/ratelimiter.lua";
    private static String rateLimiterScript;

    static {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(RATE_LIMITER_FILE_PATH);
        try {
            rateLimiterScript =  StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("ratelimiter Initialization failure",e);
        }
    }

    public static String getRateLimiterScript() {
        return rateLimiterScript;
    }
}
