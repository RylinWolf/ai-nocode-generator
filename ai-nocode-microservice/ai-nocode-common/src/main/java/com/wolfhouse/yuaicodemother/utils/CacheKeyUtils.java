package com.wolfhouse.yuaicodemother.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * @author Rylin Wolf
 */
public class CacheKeyUtils {
    /**
     * 根据输入对象生成缓存键的工具方法。
     *
     * @param object 输入对象，用于生成缓存键，如果为 null，则返回 "null" 的 MD5 哈希值。
     * @return 根据输入对象生成的 MD5 哈希值，作为缓存键。
     */
    public static String generateKey(Object object) {
        if (object == null) {
            return DigestUtil.md5Hex("null");
        }
        // 转成 Json
        String jsonStr = JSONUtil.toJsonStr(object);
        return DigestUtil.md5Hex(jsonStr);
    }
}
