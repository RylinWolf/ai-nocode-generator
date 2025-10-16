package com.wolfhouse.yuaicodemother.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用更新请求（管理员）
 *
 * @author rylinwolf
 */
@Data
public class AppUpdateByAdminRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 应用封面
     */
    private String cover;
    /**
     * 优先级
     */
    private Integer priority;
}
