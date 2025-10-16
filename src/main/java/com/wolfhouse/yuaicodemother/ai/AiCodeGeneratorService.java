package com.wolfhouse.yuaicodemother.ai;

import com.wolfhouse.yuaicodemother.ai.model.HtmlCodeResult;
import com.wolfhouse.yuaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
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
}
