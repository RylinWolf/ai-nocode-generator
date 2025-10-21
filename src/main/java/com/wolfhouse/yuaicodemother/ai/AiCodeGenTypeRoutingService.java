package com.wolfhouse.yuaicodemother.ai;

import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * @author Rylin Wolf
 */
public interface AiCodeGenTypeRoutingService {
    /**
     * 根据用户输入的提示词，路由并返回适当的代码生成类型。
     *
     * @param userPrompt 用户输入的提示词，用于指明生成代码的需求或上下文。
     * @return 对应的代码生成类型枚举 CodeGenTypeEnum，如果无法匹配返回 null。
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
}
