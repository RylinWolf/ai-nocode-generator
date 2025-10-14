package com.wolfhouse.yuaicodemother.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除请求包装类
 *
 * @author rylinwolf
 */
@Data
public class DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
}
