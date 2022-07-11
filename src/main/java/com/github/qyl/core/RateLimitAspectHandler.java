package com.github.qyl.core;

import com.github.qyl.RateLimiterProperties;
import com.github.qyl.annotation.RateLimit;
import com.github.qyl.constant.RateLimitModel;
import com.github.qyl.exception.RateLimitException;
import com.github.qyl.model.LuaScript;
import com.github.qyl.model.RateLimiterInfo;
import com.github.qyl.model.TokenBucketLuaScript;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
@Order(0)
@Slf4j
@ConditionalOnProperty(prefix = RateLimiterProperties.PREFIX, name = "enabled", havingValue = "true")
public class RateLimitAspectHandler {

    private final RateLimiterService rateLimiterService;
    private final RScript rScript;

    public RateLimitAspectHandler(RedissonClient client, RateLimiterService lockInfoProvider) {
        this.rateLimiterService = lockInfoProvider;
        this.rScript = client.getScript();
    }

    @Around(value = "@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        RateLimiterInfo limiterInfo = rateLimiterService.getRateLimiterInfo(joinPoint, rateLimit);

        if (limiterInfo.getModel().equals(RateLimitModel.COUNT.type())) {
            List<Object> keys = new ArrayList<>();
            keys.add(limiterInfo.getKey());
            keys.add(limiterInfo.getRate());
            keys.add(limiterInfo.getRateInterval());
            List<Long> results = rScript.eval(RScript.Mode.READ_WRITE, LuaScript.getRateLimiterScript(), RScript.ReturnType.MULTI, keys);
            boolean allowed =
                    results.get(0) == 0L;
            if (!allowed) {
                if (StringUtils.hasLength(rateLimit.fallbackFunction())) {
                    return rateLimiterService.executeFunction(rateLimit.fallbackFunction(), joinPoint);
                }
                long ttl = results.get(1);
                log.info("Trigger current limiting, key:{}", limiterInfo.getKey());
                throw new RateLimitException("Too Many Requests", ttl);
            }
        } else {
            List<Object> keys = new ArrayList<>();
            //标识
            keys.add(limiterInfo.getKey());
            //令牌桶最大容量
            keys.add(limiterInfo.getMaxQuantity());
            //时间窗口内的限额
            keys.add(limiterInfo.getRate());
            //时间窗口大小（秒）
            keys.add(limiterInfo.getRateInterval());
            //请求的令牌数量
            keys.add(limiterInfo.getQuantity());
            List<Long> results = rScript.eval(RScript.Mode.READ_WRITE, TokenBucketLuaScript.getTokenBucketLuaScript(), RScript.ReturnType.MULTI, keys);
            boolean allowed =
                    results.get(0) == 0L;
            if (!allowed) {
                Map<String, String> map = new HashMap<String, String>(4);
                map.put("Current Limiting", results.get(0) == 0 ? "NO" :"YES");
                map.put("Total Quantity", results.get(1).toString());
                map.put("Available Capacity", results.get(2).toString());
                map.put("Apply for Quantity", String.valueOf(limiterInfo.getQuantity()));
                log.info("Trigger current limiting, key:{}, info: {}", limiterInfo.getKey(), map);
                if (StringUtils.hasLength(rateLimit.fallbackFunction())) {
                    return rateLimiterService.executeFunction(rateLimit.fallbackFunction(), joinPoint);
                }
                throw new RateLimitException("Too Many Requests", map);
            }
        }
        return joinPoint.proceed();
    }
}
