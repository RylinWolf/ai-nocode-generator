package com.wolfhouse.yuaicodemother.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linexsong
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StreamMessage {
    /** 消息类型 */
    private String type;
}
