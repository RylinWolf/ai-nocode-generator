package com.wolfhouse.yuaicodeapp.core.parser;

/**
 * @author linexsong
 */
public interface CodeParser<T> {
    /**
     * 解析给定的代码字符串并返回对应的对象。
     *
     * @param code 要解析的代码字符串
     * @return 解析后的对象
     */
    T parseCode(String code);
}
