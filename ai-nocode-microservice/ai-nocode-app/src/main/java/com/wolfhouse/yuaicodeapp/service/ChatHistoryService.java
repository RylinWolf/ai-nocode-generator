package com.wolfhouse.yuaicodeapp.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wolfhouse.yuaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.wolfhouse.yuaicodemother.model.entity.ChatHistory;
import com.wolfhouse.yuaicodemother.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/rylinwolf">Rylin</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 添加一条聊天消息记录。
     *
     * @param appId       应用的唯一标识 ID
     * @param message     聊天内容
     * @param messageType 消息类型（例如 "user" 或 "ai"）
     * @param userId      创建消息的用户 ID
     * @return 如果消息添加成功，返回 true，否则返回 false
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用 ID 删除对话记录
     *
     * @param appId 应用 ID
     * @return 是否成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 分页查询指定应用的对话历史记录。
     *
     * @param appId          应用的唯一标识 ID
     * @param pageSize       每页显示的记录数
     * @param lastCreateTime 上次查询的最后一条记录的创建时间，用于分页查询
     * @param loginUser      当前登录用户的信息，用于权限验证
     * @return 包含对话历史记录的分页结果
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 加载指定应用的聊天历史记录到内存。
     *
     * @param appId      应用的唯一标识 ID
     * @param chatMemory 聊天窗口内存，用于存储加载的聊天记录
     * @param maxCount   最大加载记录数，限制加载到内存中的记录数量
     * @return 实际加载的聊天记录数量
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 构建查询条件
     *
     * @param chatHistoryQueryRequest 查询条件 Dto
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
