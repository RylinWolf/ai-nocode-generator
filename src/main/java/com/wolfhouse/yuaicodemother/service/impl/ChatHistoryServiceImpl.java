package com.wolfhouse.yuaicodemother.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wolfhouse.yuaicodemother.mapper.ChatHistoryMapper;
import com.wolfhouse.yuaicodemother.model.entity.ChatHistory;
import com.wolfhouse.yuaicodemother.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/rylinwolf">Rylin</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

}
