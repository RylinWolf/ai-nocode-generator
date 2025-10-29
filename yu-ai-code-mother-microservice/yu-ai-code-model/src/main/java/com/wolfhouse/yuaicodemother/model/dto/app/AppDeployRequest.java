package com.wolfhouse.yuaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 部署请求
 *
 * @author rylinwolf
 */
@Data
public class AppDeployRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 应用 id
     */
    private Long appId;
}
