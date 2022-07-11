package com.github.qyl.annotation;


import com.github.qyl.constant.RateLimitModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author : qi yu liu
 * @Create 2021/6/8
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 时间窗口流量数量
     * @return rate
     */
    long rate();

    /**
     * 时间窗口流量数量表达式
     * @return rateExpression
     */
    String rateExpression() default "";

    /**
     * 时间窗口，最小单位秒，如 2s，2h , 2d
     * @return rateInterval
     */
    String rateInterval();

    /**
     * 获取key
     * @return keys
     */
    String [] keys() default {};

    /**
     * 限流后的自定义回退后的拒绝逻辑
     * @return fallback
     */
    String fallbackFunction() default "";

    /**
     * 自定义业务 key 的 Function
     * @return key
     */
    String customKeyFunction() default "";

    /**
     * 每次请求令牌的数量(令牌桶模式参数) 默认为1
     * @return quantity
     */
    long quantity() default 1L;

    /**
     * 令牌桶的容量(令牌桶模式参数) 默认为10
     * @return quantity
     */
    long maxQuantity() default 10L;

    /**
     * 限流模式 默认是固定时间计数器模式
     * @return
     */
    RateLimitModel model() default RateLimitModel.COUNT;




}
