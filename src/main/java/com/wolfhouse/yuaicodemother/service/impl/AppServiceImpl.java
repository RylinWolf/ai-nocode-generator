package com.wolfhouse.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.exception.ThrowUtils;
import com.wolfhouse.yuaicodemother.mapper.AppMapper;
import com.wolfhouse.yuaicodemother.model.dto.AppQueryRequest;
import com.wolfhouse.yuaicodemother.model.entity.App;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.vo.AppVO;
import com.wolfhouse.yuaicodemother.model.vo.UserVO;
import com.wolfhouse.yuaicodemother.service.AppService;
import com.wolfhouse.yuaicodemother.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wolfhouse.yuaicodemother.model.entity.table.AppTableDef.APP;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/rylinwolf">Rylin</a>
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final UserService userService;

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                           .where(APP.ID.eq(id))
                           .and(APP.APP_NAME.like(appName))
                           .and(APP.COVER.like(cover))
                           .and(APP.INIT_PROMPT.like(initPrompt))
                           .and(APP.CODE_GEN_TYPE.eq(codeGenType))
                           .and(APP.DEPLOY_KEY.eq(deployKey))
                           .and(APP.PRIORITY.eq(priority))
                           .and(APP.USER_ID.eq(userId))
                           .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public void validApp(App app, boolean add) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        String initPrompt = app.getInitPrompt();

        // 创建时，必填字段校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        }

        // 有参数则校验
        if (StrUtil.isNotBlank(initPrompt)) {
            ThrowUtils.throwIf(initPrompt.length() > 5000, ErrorCode.PARAMS_ERROR, "初始化 prompt 过长");
        }
    }

    @Override
    public AppVO getAppVo(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            appVO.setUser(BeanUtil.copyProperties(user, UserVO.class));
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                                   .map(App::getUserId)
                                   .collect(Collectors.toSet());
        Map<Long, UserVO> userVoMap = userService.listByIds(userIds)
                                                 .stream()
                                                 .collect(Collectors.toMap(User::getId, userService::getUserVo));
        return appList.stream()
                      .map(app -> {
                          AppVO appVO = BeanUtil.copyProperties(app, AppVO.class);
                          UserVO userVO = userVoMap.get(app.getUserId());
                          appVO.setUser(userVO);
                          return appVO;
                      })
                      .collect(Collectors.toList());
    }

}
