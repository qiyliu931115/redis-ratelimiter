package com.github.qyl.constant;

/**
 * @Author : qi yu liu
 * @Create 2021/6/9
 */

public enum RateLimitModel {

    /**
     * 固定时间计数器
     */
    COUNT("COUNT", "固定时间计数器"),

    /**
     * 令牌桶
     */
    TOKEN_BUCKET("TOKEN_BUCKET", "令牌桶"),

    ;

    /**
     * 类型
     */
    private String type;
    /**
     * 描述
     */
    private String desc;

    RateLimitModel(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static String getValue(String type) {
        RateLimitModel[] rateLimitModels = values();
        for (RateLimitModel rateLimitModel : rateLimitModels) {
            if (rateLimitModel.type().equals(type)) {
                return rateLimitModel.desc();
            }
        }
        return null;
    }

    public static String getType(String desc) {
        RateLimitModel[] rateLimitModels = values();
        for (RateLimitModel rateLimitModel : rateLimitModels) {
            if (rateLimitModel.type().equals(desc)) {
                return rateLimitModel.type();
            }
        }
        return null;
    }

    public String type() {
        return this.type;
    }

    public String desc() {
        return this.desc;
    }

}
