package com.wolfhouse.yuaicodemother.ai;

import com.wolfhouse.yuaicodemother.ai.model.HtmlCodeResult;
import com.wolfhouse.yuaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * @author linexsong
 */
public interface AiCodeGeneratorService {
    /**
     * 生成代码
     *
     * @param userMessage 用户提示词
     * @return 输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateCode(String userMessage);

    /**
     * 根据用户输入的提示词生成多文件代码。
     *
     * @param userMessage 用户的提示信息，描述生成代码的需求。
     * @return 返回生成的多文件代码，以字符串形式表示。
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成代码
     *
     * @param userMessage 用户提示词
     * @return 输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateCodeStream(String userMessage);

    /**
     * 根据用户输入的提示词生成多文件代码。
     *
     * @param userMessage 用户的提示信息，描述生成代码的需求。
     * @return 返回生成的多文件代码，以字符串形式表示。
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);


    /**
     * 根据指定的应用 ID 和用户输入的消息生成 Vue 项目代码，并以流式响应的方式输出结果。
     *
     * @param appId       应用的唯一标识 ID，用于关联和加载上下文信息。
     * @param userMessage 用户输入的提示信息，描述需要生成的 Vue 项目代码的具体需求。
     * @return 返回生成的 Vue 项目代码流，每个元素表示一段代码片段。
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    Flux<String> generateVueProjectCodeStream(@MemoryId Long appId, @UserMessage String userMessage);
}
