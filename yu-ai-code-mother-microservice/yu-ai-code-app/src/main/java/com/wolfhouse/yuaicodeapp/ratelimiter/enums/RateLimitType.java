package com.wolfhouse.yuaicodeapp.ratelimiter.enums;

/**
 * @author Rylin Wolf
 */
public enum RateLimitType {

    /**
     * 接口级别限流
     */
    API,

    /**
     * 用户级别限流
     */
    USER,

    /**
     * IP级别限流
     */
    IP
}

