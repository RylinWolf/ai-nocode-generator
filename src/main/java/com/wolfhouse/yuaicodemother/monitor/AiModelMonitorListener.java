package com.wolfhouse.yuaicodemother.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * AI 模型监听器
 *
 * @author Rylin Wolf
 */
@Component
public class AiModelMonitorListener implements ChatModelListener {
    private static final String REQUEST_START_TIME_KEY = "request_start_time";
    // 用于监控上下文传递（请求和响应事件的触发不是同一个线程）
    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // 保存起始时间
        requestContext.attributes()
                      .put(REQUEST_START_TIME_KEY, Instant.now());
        // 从监控上下文获取信息
        MonitorContext context = MonitorContextHolder.getContext();
        // 将上下文保存至请求状态，用于后续的响应传递
        // 因为 AI 响应时的线程与请求线程不是同一个线程
        requestContext.attributes()
                      .put(MONITOR_CONTEXT_KEY, context);
        String appId = context.getAppId();
        String userId = context.getUserId();
        String modelName = requestContext.chatRequest()
                                         .modelName();
        // 记录请求指标
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");


    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        Map<Object, Object> attributes = responseContext.attributes();
        // 获取上下文
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
        String appId = context.getAppId();
        String userId = context.getUserId();
        String modelName = responseContext.chatResponse()
                                          .modelName();
        TokenUsage tokenUsage = responseContext.chatResponse()
                                               .metadata()
                                               .tokenUsage();
        // 记录成功请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
        recordResponseTime(attributes, userId, appId, modelName);
        recordTokenUsage(tokenUsage, userId, appId, modelName);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        MonitorContext context = MonitorContextHolder.getContext();
        String appId = context.getAppId();
        String userId = context.getUserId();
        // 获取模型名称和错误类型
        String modelName = errorContext.chatRequest()
                                       .modelName();
        String errorMessage = errorContext.error()
                                          .getMessage();
        // 记录失败请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);
        // 记录响应时间
        Map<Object, Object> attributes = errorContext.attributes();
        recordResponseTime(attributes, userId, appId, modelName);
    }

    /**
     * 记录响应时间
     */
    private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
        Duration responseTime = Duration.between(startTime, Instant.now());
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
    }

    /**
     * 记录Token使用情况
     */
    private void recordTokenUsage(TokenUsage tokenUsage,
                                  String userId,
                                  String appId,
                                  String modelName) {
        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
        }
    }
}

