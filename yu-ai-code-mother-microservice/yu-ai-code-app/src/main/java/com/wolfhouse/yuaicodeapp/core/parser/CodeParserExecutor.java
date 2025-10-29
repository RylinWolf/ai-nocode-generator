package com.wolfhouse.yuaicodeapp.core.parser;

import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器执行器
 *
 * @author linexsong
 */
public class CodeParserExecutor {
    private static final HtmlCodeParser HTML_CODE_PARSER = new HtmlCodeParser();
    private static final MultiFileCodeParser MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 根据传入的代码内容和代码生成类型枚举，执行相应的代码解析操作。
     *
     * @param codeContent 待解析的代码内容
     * @param genTypeEnum 代码生成类型枚举，决定采用何种解析器（例如 HTML、MULTI_FILE）
     * @return 解析后的对象，返回值类型取决于解析器的具体实现
     */
    public static Object execute(String codeContent, CodeGenTypeEnum genTypeEnum) {
        return switch (genTypeEnum) {
            case HTML -> HTML_CODE_PARSER.parseCode(codeContent);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的类型");
        };
    }
}
