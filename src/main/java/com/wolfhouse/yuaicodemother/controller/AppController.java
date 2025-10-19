package com.wolfhouse.yuaicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wolfhouse.yuaicodemother.common.BaseResponse;
import com.wolfhouse.yuaicodemother.common.DeleteRequest;
import com.wolfhouse.yuaicodemother.common.ResultUtils;
import com.wolfhouse.yuaicodemother.common.annotation.AuthCheck;
import com.wolfhouse.yuaicodemother.common.constant.AppConstant;
import com.wolfhouse.yuaicodemother.common.constant.UserConstant;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.exception.ThrowUtils;
import com.wolfhouse.yuaicodemother.model.dto.app.*;
import com.wolfhouse.yuaicodemother.model.entity.App;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.wolfhouse.yuaicodemother.model.enums.UserRoleEnum;
import com.wolfhouse.yuaicodemother.model.vo.AppVO;
import com.wolfhouse.yuaicodemother.service.AppService;
import com.wolfhouse.yuaicodemother.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.wolfhouse.yuaicodemother.model.entity.table.AppTableDef.APP;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/rylinwolf">Rylin</a>
 */
@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;
    private final UserService userService;

    // region 用户操作

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        return contentFlux.map(chunk -> {
                              Map<String, String> map = Map.of("d", chunk);
                              String jsonStr = JSONUtil.toJsonStr(map);
                              return ServerSentEvent.<String>builder()
                                                    .data(jsonStr)
                                                    .build();
                          })
                          .concatWith(Mono.just(
                              // 发送结束事件
                              ServerSentEvent.<String>builder()
                                             .event("done")
                                             .data("")
                                             .build()));
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }


    /**
     * 创建应用
     *
     * @param appAddRequest 创建请求
     * @param request       请求
     * @return 新应用 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest,
                                     HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);

        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);

        // 参数校验
        appService.validApp(app, true);

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        app.setUserId(loginUser.getId());
        // 暂时为 initPrompt 的前 12 位
        app.setAppName(app.getInitPrompt()
                          .length() > 12 ?
                       app.getInitPrompt()
                          .substring(0, 12) :
                       app.getInitPrompt());
        // 文件生成模式
        app.setCodeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue());

        // 保存
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    /**
     * 根据 id 修改自己的应用（只能修改应用名称）
     *
     * @param appUpdateRequest 更新请求
     * @param request          请求
     * @return 是否成功
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest,
                                           HttpServletRequest request) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        long id = appUpdateRequest.getId();

        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        // 仅本人可修改
        if (!oldApp.getUserId()
                   .equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());
        app.setEditTime(LocalDateTime.now());
        // 参数校验
        appService.validApp(app, false);

        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 删除自己的应用
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest,
                                           HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();

        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        // 仅本人或管理员可删除
        if (!oldApp.getUserId()
                   .equals(loginUser.getId()) && !UserRoleEnum.ADMIN.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 查看应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVoById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVo(app));
    }

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 分页结果
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVoByPage(@RequestBody AppQueryRequest appQueryRequest,
                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        appQueryRequest.setUserId(loginUser.getId());

        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();

        // 限制每页最多 20 个
        if (pageSize > 20) {
            pageSize = 20;
        }
        // 只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());

        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                                            appService.getQueryWrapper(appQueryRequest));

        Page<AppVO> appVoPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVoList = appService.getAppVOList(appPage.getRecords());
        appVoPage.setRecords(appVoList);
        return ResultUtils.success(appVoPage);
    }

    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listFeaturedAppVoByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();

        // 限制每页最多 20 个
        if (pageSize > 20) {
            pageSize = 20;
        }

        // 查询精选应用（优先级大于 99）
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        queryWrapper.and(APP.PRIORITY.ge(AppConstant.GOOD_APP_PRIORITY));

        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<AppVO> appVoPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVoList = appService.getAppVOList(appPage.getRecords());
        appVoPage.setRecords(appVoList);
        return ResultUtils.success(appVoPage);
    }

    // endregion

    // region 管理员操作

    /**
     * 根据 id 删除任意应用（管理员）
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/admin/delete")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 更新任意应用（管理员）
     *
     * @param appUpdateByAdminRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/admin/update")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppUpdateByAdminRequest appUpdateByAdminRequest) {
        if (appUpdateByAdminRequest == null || appUpdateByAdminRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断是否存在
        App oldApp = appService.getById(appUpdateByAdminRequest.getId());
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        App app = new App();
        BeanUtil.copyProperties(appUpdateByAdminRequest, app);

        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());

        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页查询应用列表（管理员）
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVoByPageAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();

        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                                            appService.getQueryWrapper(appQueryRequest));

        Page<AppVO> appVoPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVoList = BeanUtil.copyToList(appPage.getRecords(), AppVO.class);
        appVoPage.setRecords(appVoList);
        return ResultUtils.success(appVoPage);
    }

    /**
     * 根据 id 查看应用详情（管理员）
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(app);
    }

}
