package com.wolfhouse.yuaicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 流式对话模型配置
 *
 * @author linexsong
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Integer maxTokens;
    private Double temperature;
    private boolean logRequests;
    private boolean logResponses;

    /**
     * 推理流式模型（用于 Vue 生成）
     *
     * @return 流式推理模型
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel streamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                                       .apiKey(apiKey)
                                       .baseUrl(baseUrl)
                                       .modelName(modelName)
                                       .maxTokens(maxTokens)
                                       .temperature(temperature)
                                       .logRequests(true)
                                       .logResponses(true)
                                       .build();
    }
}
