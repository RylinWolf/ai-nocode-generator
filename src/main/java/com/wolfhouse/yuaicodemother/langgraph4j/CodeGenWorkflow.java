package com.wolfhouse.yuaicodemother.langgraph4j;

import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.langgraph4j.model.QualityResult;
import com.wolfhouse.yuaicodemother.langgraph4j.node.*;
import com.wolfhouse.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * 代码生成工作流
 *
 * @author Rylin Wolf
 */
@Slf4j
public class CodeGenWorkflow {

    /**
     * 创建完整的工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                // 添加节点 - 使用完整实现的节点
                .addNode("image_collector", ImageCollectorNode.create())
                .addNode("prompt_enhancer", PromptEnhancerNode.create())
                .addNode("router", RouterNode.create())
                .addNode("code_generator", CodeGeneratorNode.create())
                .addNode("code_quality_check", CodeQualityCheckNode.create())
                .addNode("project_builder", ProjectBuilderNode.create())

                // 添加边
                .addEdge(START, "image_collector")
                .addEdge("image_collector", "prompt_enhancer")
                .addEdge("prompt_enhancer", "router")
                .addEdge("router", "code_generator")
                .addEdge("code_generator", "code_quality_check")
                .addConditionalEdges("code_quality_check",
                                     AsyncEdgeAction.edge_async(this::routeAfterQualityCheck),
                                     Map.of(
                                         // 质检通过但需要构建
                                         "build", "project_builder",
                                         // 质检通过无需构建
                                         "skip_build", END,
                                         // 质检不通过，重新生成
                                         "fail", "code_generator"))
                .addEdge("project_builder", END)

                // 编译工作流
                .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();
        // 如果质检失败，重新生成代码
        if (qualityResult == null || !qualityResult.getIsValid()) {
            log.error("代码质检失败，需要重新生成代码");
            return "fail";
        }
        // 质检通过，使用原有的构建路由逻辑
        log.info("代码质检通过，继续后续流程");
        return routeBuildOrSkip(state);
    }


    private String routeBuildOrSkip(MessagesState<String> stringMessagesState) {
        WorkflowContext context = WorkflowContext.getContext(stringMessagesState);
        CodeGenTypeEnum generationType = context.getGenerationType();
        // 不需要构建
        if (generationType == CodeGenTypeEnum.MULTI_FILE || generationType == CodeGenTypeEnum.HTML) {
            return "skip_build";
        }
        // Vue 项目需要构建
        return "build";
    }

    /**
     * 执行工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化 WorkflowContext
        WorkflowContext initialContext = WorkflowContext.builder()
                                                        .originalPrompt(originalPrompt)
                                                        .currentStep("初始化")
                                                        .build();

        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());
        log.info("开始执行代码生成工作流");

        WorkflowContext finalContext = null;
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
            Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("代码生成工作流执行完成！");
        return finalContext;
    }
}
