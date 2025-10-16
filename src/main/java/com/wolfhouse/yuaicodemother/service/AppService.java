package com.wolfhouse.yuaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wolfhouse.yuaicodemother.model.dto.app.AppQueryRequest;
import com.wolfhouse.yuaicodemother.model.entity.App;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

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

    AppVO getAppVo(App app);

    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 通过对话生成应用代码
     *
     * @param appId       应用 ID
     * @param userMessage 提示词
     * @param loginUser   登录用户
     * @return 响应流
     */
    Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser);

    /**
     * 部署指定的应用。
     *
     * @param appId     应用的唯一标识 ID
     * @param loginUser 当前登录用户的信息
     * @return 部署的结果信息
     */
    String deployApp(Long appId, User loginUser);
}
