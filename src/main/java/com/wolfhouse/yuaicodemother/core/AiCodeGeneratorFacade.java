package com.wolfhouse.yuaicodemother.core;

import cn.hutool.json.JSONUtil;
import com.wolfhouse.yuaicodemother.ai.AiCodeGeneratorService;
import com.wolfhouse.yuaicodemother.ai.AiCodeGeneratorServiceFactory;
import com.wolfhouse.yuaicodemother.ai.model.message.AiResponseMessage;
import com.wolfhouse.yuaicodemother.ai.model.message.ToolExecutedMessage;
import com.wolfhouse.yuaicodemother.ai.model.message.ToolRequestMessage;
import com.wolfhouse.yuaicodemother.common.constant.AppConstant;
import com.wolfhouse.yuaicodemother.core.builder.VueProjectBuilder;
import com.wolfhouse.yuaicodemother.core.parser.CodeParserExecutor;
import com.wolfhouse.yuaicodemother.core.saver.CodeFileSaverExecutor;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Path;

/**
 * AI 代码生成门面类，组合代码生成和保存功能
 *
 * @author linexsong
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AiCodeGeneratorFacade {
    private final AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    private final VueProjectBuilder vueProjectBuilder;

    /**
     * 根据用户提供的信息生成代码并将其保存到文件中。
     *
     * @param userMessage 用户提供的输入信息，用于描述需要生成的代码内容。
     * @return 返回保存生成代码的文件对象。
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum genTypeEnum, Long appId) {
        if (genTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取响应的 Ai服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,
                                                                                                                genTypeEnum);
        return switch (genTypeEnum) {
            case HTML -> CodeFileSaverExecutor.executeSaver(aiCodeGeneratorService.generateCode(userMessage),
                                                            genTypeEnum, appId);
            case MULTI_FILE ->
                CodeFileSaverExecutor.executeSaver(aiCodeGeneratorService.generateMultiFileCode(userMessage),
                                                   genTypeEnum, appId);
            default ->
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型: " + genTypeEnum.getValue());
        };
    }

    /**
     * 根据用户提供的信息生成代码并将其保存到文件中。（流式）
     *
     * @param userMessage 用户提供的输入信息，用于描述需要生成的代码内容。
     * @return 返回保存生成代码的文件对象。
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum genTypeEnum, Long appId) {
        if (genTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取响应的 Ai服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,
                                                                                                                genTypeEnum);

        return switch (genTypeEnum) {
            case HTML -> processCodeStream(aiCodeGeneratorService.generateCodeStream(userMessage),
                                           CodeGenTypeEnum.HTML, appId);
            case MULTI_FILE -> processCodeStream(aiCodeGeneratorService.generateMultiFileCodeStream(userMessage),
                                                 CodeGenTypeEnum.MULTI_FILE, appId);
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processCodeStream(processTokenStream(tokenStream, appId),
                                        CodeGenTypeEnum.MULTI_FILE, appId);
            }

            default ->
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型: " + genTypeEnum.getValue());
        };
    }

    /**
     * 处理 TokenStream 对象并将其转换为字符串流。
     * （适配器模式）
     *
     * @param tokenStream 输入的 TokenStream 对象。
     * @param appId       应用 ID
     * @return 处理后的字符串流。
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                           AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                           sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                       })
                       .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                           ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                           sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                       })
                       .onToolExecuted((ToolExecution toolExecution) -> {
                           ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                           sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                       })
                       .onCompleteResponse((ChatResponse response) -> {
                           // 同步构建 vue 项目
                           String projectPath = Path.of(AppConstant.CODE_OUTPUT_ROOT_DIR, "vue_project_" + appId)
                                                    .toString();
                           vueProjectBuilder.buildProjectAsync(projectPath);
                           sink.complete();
                       })
                       .onError((Throwable error) -> {
                           log.error(error.getMessage(), error);
                           sink.error(error);
                       })
                       // 开始监听
                       .start();
        });
    }


    /**
     * 生成不同文件模式的代码并保存（流式）
     *
     * @param codeStream 代码流
     * @param typeEnum   代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum typeEnum, Long appId) {
        //  字符串拼接器，用于流式返回所有代码之后，再保存代码
        StringBuilder stringBuilder = new StringBuilder();
        return codeStream.doOnNext(stringBuilder::append)
                         .doOnComplete(() -> {
                             try {
                                 String code = stringBuilder.toString();
                                 // 使用执行器解析代码
                                 Object parsedResult = CodeParserExecutor.execute(code, typeEnum);
                                 // 使用执行器保存代码
                                 File file = CodeFileSaverExecutor.executeSaver(parsedResult, typeEnum, appId);
                                 log.info("文件保存成功，目录为: {}", file.getAbsolutePath());
                             } catch (Exception e) {
                                 log.error("保存失败", e);
                             }
                         });
    }
}
