package com.wolfhouse.yuaicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wolfhouse.yuaicodemother.ai.tools.ToolManager;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.wolfhouse.yuaicodemother.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 服务创建工厂
 *
 * @author linexsong
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AiCodeGeneratorServiceFactory {
    private final RedisChatMemoryStore redisChatMemoryStore;
    private final ChatHistoryService chatHistoryService;
    private final ChatModel chatModel;
    private final ToolManager toolManager;
    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<@NonNull String, AiCodeGeneratorService> serviceCache =
        Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(10))
                .removalListener((key, value, cause) -> {
                    log.debug(
                        "AI 服务实例被移除，缓存键: {}, 原因: {}",
                        key,
                        cause);
                })
                .build();
    private StreamingChatModel reasoningStreamingChatModel;
    private StreamingChatModel streamingChatModel;

    @Autowired
    public void setChatModel(@Qualifier("openAiStreamingChatModel") StreamingChatModel chatModel) {
        this.streamingChatModel = chatModel;
    }

    @Autowired
    public void setReasoningStreamingChatModel(
        @Qualifier("reasoningStreamingChatModel") StreamingChatModel reasoningStreamingChatModel) {
        this.reasoningStreamingChatModel = reasoningStreamingChatModel;
    }

    /**
     * 根据 appId 获取服务（带缓存）
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum genType) {
        return serviceCache.get(buildCacheKey(appId, genType), (k) -> createAiCodeGeneratorService(appId, genType));
    }

    /**
     * 创建新的 AI 服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum genType) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .id(appId)
            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(100)
            .build();
        // 从数据库中加载对话历史到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);

        switch (genType) {
            // Vue 项目生成，使用工具调用和推理模型
            case VUE_PROJECT -> {
                return AiServices.builder(AiCodeGeneratorService.class)
                                 .streamingChatModel(reasoningStreamingChatModel)
                                 .chatMemoryProvider((id) -> chatMemory)
                                 .tools((Object[]) toolManager.getAllTools())
                                 // 处理工具调用幻觉问题
                                 .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                                                   ToolExecutionResultMessage.from(
                                                                       toolExecutionRequest,
                                                                       "错误：没有该名称的工具 " +
                                                                       toolExecutionRequest.name()))
                                 .build();
            }
            // 普通调用
            case HTML, MULTI_FILE -> {
                return AiServices.builder(AiCodeGeneratorService.class)
                                 .chatModel(chatModel)
                                 .streamingChatModel(streamingChatModel)
                                 .chatMemory(chatMemory)
                                 .build();
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型: " + genType);
        }

    }

    /**
     * 创建 AI 代码生成器服务
     *
     * @return Ai 代码生成器服务
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }

    /**
     * 构建缓存键的方法。
     *
     * @param appId   应用的唯一标识 ID
     * @param genType 代码生成类型的枚举值
     * @return 生成的缓存键字符串，由应用 ID 和生成类型拼接而成
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum genType) {
        return appId + "_" + genType.name();
    }
}
