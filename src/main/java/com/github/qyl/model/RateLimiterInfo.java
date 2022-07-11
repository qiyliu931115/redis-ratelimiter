package com.github.qyl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterInfo {

    /**
     * key
     */
    private String key;

    /**
     * 时间窗口流量数量
     */
    private long rate;

    /**
     * 时间窗口，最小单位秒，如 2s，2h , 2d
     */
    private long rateInterval;

    /**
     * 每次请求令牌的数量(令牌桶模式参数)
     */
    private long quantity;

    /**
     * 令牌桶的容量(令牌桶模式参数)
     * @return quantity
     */
    private long maxQuantity;

    /**
     * 模式
     */
    private String model;



}
