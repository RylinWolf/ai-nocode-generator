package com.wolfhouse.yuaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wolfhouse.yuaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.wolfhouse.yuaicodemother.model.entity.ChatHistory;
import com.wolfhouse.yuaicodemother.model.entity.User;

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

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 构建查询条件
     *
     * @param chatHistoryQueryRequest 查询条件 Dto
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
