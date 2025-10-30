package com.wolfhouse.yuaicodemother.ai.config;

import com.wolfhouse.yuaicodemother.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * @author linexsong
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {
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
    public StreamingChatModel reasoningStreamingChatModelPrototype(AiModelMonitorListener listener) {
        return OpenAiStreamingChatModel.builder()
                                       .apiKey(apiKey)
                                       .baseUrl(baseUrl)
                                       .modelName(modelName)
                                       .maxTokens(maxTokens)
                                       .temperature(temperature)
                                       .logRequests(true)
                                       .logResponses(true)
                                       .listeners(List.of(listener))
                                       .build();
    }
}
