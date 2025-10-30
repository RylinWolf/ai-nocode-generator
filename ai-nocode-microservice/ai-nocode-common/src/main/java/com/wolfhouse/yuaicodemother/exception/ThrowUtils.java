package com.wolfhouse.yuaicodemother.exception;

/**
 * @author linexsong
 */
public class ThrowUtils {
    /**
     * 条件成立则抛出异常
     *
     * @param condition 条件
     * @param e         异常
     */
    public static void throwIf(boolean condition, RuntimeException e) {
        if (condition) {
            throw e;
        }
    }

    /**
     * 条件成立则抛出异常
     *
     * @param condition 条件
     * @param errorCode 异常代码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 条件成立则抛出异常
     *
     * @param condition 条件
     * @param errorCode 异常代码
     * @param message   错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BusinessException(errorCode, message);
        }
    }
}
