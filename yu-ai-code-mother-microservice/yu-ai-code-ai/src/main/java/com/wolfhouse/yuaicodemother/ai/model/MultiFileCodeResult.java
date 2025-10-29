package com.wolfhouse.yuaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 表示生成多个代码文件的结果。
 * 此类封装了 HTML、CSS、JS 代码文件的内容和相关描述信息。
 * <p>
 * 本类主要用于 AI 多文件代码生成服务的结果返回，例如
 * {@link com.wolfhouse.yuaicodemother.ai.AiCodeGeneratorService#generateMultiFileCode(String)}。
 * 通过此类，可以接收生成的多个代码文件内容，并在生成器服务中进一步处理。
 */
@Description("生成多个代码文件的结果")
@Data
public class MultiFileCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("CSS代码")
    private String cssCode;

    @Description("JS代码")
    private String jsCode;

    @Description("生成代码的描述")
    private String description;
}
