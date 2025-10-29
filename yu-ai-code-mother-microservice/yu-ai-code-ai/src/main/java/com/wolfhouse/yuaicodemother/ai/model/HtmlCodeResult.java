package com.wolfhouse.yuaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 表示生成 HTML 代码文件的结果。
 * 此类封装了生成的 HTML 代码以及相关描述信息。
 * <p>
 * 本类主要用于 AI 代码生成服务（如 {@link com.wolfhouse.yuaicodemother.ai.AiCodeGeneratorService}）的结果返回。
 *
 * @author rylinwolf
 */
@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("生成代码的描述")
    private String description;
}
