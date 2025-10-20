package com.wolfhouse.yuaicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linexsong
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {
    private String baseUrl;
    private String apiKey;

    /**
     * 推理流式模型（用于 Vue 生成）
     *
     * @return 流式推理模型
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 生产模式使用
        final String modelName = "deepseek-reasoner";
        final int maxTokens = 327678;
//        final String modelName = "deepseek-chat";
//        final int maxTokens = 8192;
        return OpenAiStreamingChatModel.builder()
                                       .apiKey(apiKey)
                                       .baseUrl(baseUrl)
                                       .modelName(modelName)
                                       .maxTokens(maxTokens)
                                       .logRequests(true)
                                       .logResponses(true)
                                       .build();
    }
}
