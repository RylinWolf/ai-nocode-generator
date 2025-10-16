package com.wolfhouse.yuaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wolfhouse.yuaicodemother.model.dto.AppQueryRequest;
import com.wolfhouse.yuaicodemother.model.entity.App;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/rylinwolf">Rylin</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 校验应用参数
     *
     * @param app 应用
     * @param add 是否为创建
     */
    void validApp(App app, boolean add);
}
