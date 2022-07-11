package com.github.qyl.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @Author : qi yu liu
 * @Create 2021/6/8
 */
@Slf4j
public class TokenBucketLuaScript {

    private TokenBucketLuaScript(){}
    private static final String RATE_LIMITER_FILE_PATH = "META-INF/tokenbucket.lua";
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

    public static String getTokenBucketLuaScript() {
        return rateLimiterScript;
    }
}
