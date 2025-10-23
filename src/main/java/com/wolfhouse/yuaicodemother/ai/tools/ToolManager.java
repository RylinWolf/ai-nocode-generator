package com.wolfhouse.yuaicodemother.ai.tools;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具管理器
 * 统一管理所有工具
 *
 * @author Rylin Wolf
 */
@Slf4j
@Component
public class ToolManager {
    /** 工具名称到工具实例映射 */
    private final Map<String, BaseTool> toolMap = new HashMap<>();

    /** 自动注入所有工具 */
    private BaseTool[] tools;

    @Autowired
    public void setTools(BaseTool[] tools) {
        this.tools = tools;
    }

    /**
     * 初始化工具映射
     */
    @PostConstruct
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具: {} -> {}",
                     tool.getToolName(),
                     tool.getDisplayName());
        }
        log.info("工具管理器初始化完成，共注册 {} 个工具", toolMap.size());
    }

    /**
     * 根据工具名称获取对应的工具实例。
     *
     * @param toolName 工具的名称，用于标识所需的工具。
     * @return 对应的工具实例，如果工具名称未注册则返回 null。
     */
    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

    /**
     * 获取已注册的工具集合
     *
     * @return 工具实例集合
     */
    public BaseTool[] getAllTools() {
        return tools;
    }

}
