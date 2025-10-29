package com.wolfhouse.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wolfhouse.yuaicodemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.wolfhouse.yuaicodemother.common.constant.AppConstant;
import com.wolfhouse.yuaicodemother.core.AiCodeGeneratorFacade;
import com.wolfhouse.yuaicodemother.core.builder.VueProjectBuilder;
import com.wolfhouse.yuaicodemother.core.handler.StreamHandlerExecutor;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.exception.ThrowUtils;
import com.wolfhouse.yuaicodemother.mapper.AppMapper;
import com.wolfhouse.yuaicodemother.model.dto.app.AppAddRequest;
import com.wolfhouse.yuaicodemother.model.dto.app.AppQueryRequest;
import com.wolfhouse.yuaicodemother.model.entity.App;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.wolfhouse.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.wolfhouse.yuaicodemother.model.vo.AppVO;
import com.wolfhouse.yuaicodemother.model.vo.UserVO;
import com.wolfhouse.yuaicodemother.monitor.MonitorContext;
import com.wolfhouse.yuaicodemother.monitor.MonitorContextHolder;
import com.wolfhouse.yuaicodemother.service.AppService;
import com.wolfhouse.yuaicodemother.service.ChatHistoryService;
import com.wolfhouse.yuaicodemother.service.ScreenShotService;
import com.wolfhouse.yuaicodemother.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final UserService userService;
    private final ChatHistoryService chatHistoryService;
    private final AiCodeGeneratorFacade aiCodeGeneratorFacade;
    private final StreamHandlerExecutor streamHandlerExecutor;
    private final VueProjectBuilder vueProjectBuilder;
    private final ScreenShotService screenShotService;
    private final AiCodeGenTypeRoutingServiceFactory routingServiceFactory;

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

    @Override
    public Long createApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);

        // 参数校验
        this.validApp(app, true);

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        app.setUserId(loginUser.getId());
        // 暂时为 initPrompt 的前 12 位
        String initPrompt = app.getInitPrompt();
        app.setAppName(initPrompt
                           .length() > 12 ?
                       initPrompt
                           .substring(0, 12) :
                       initPrompt);
        // 通过 AI 智能选择代码生成类型(多例模式)
        CodeGenTypeEnum genType = routingServiceFactory.createAiCodeGenTypeRoutingService()
                                                       .routeCodeGenType(initPrompt);
        app.setCodeGenType(genType.getValue());

        // 保存
        boolean result = save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR);

        // 查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        // 权限校验，仅本人可以和自己的应用对话
        if (!app.getUserId()
                .equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问该应用");
        }

        // 获取生成模式
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");
        }

        // 保存用户消息到数据库
        chatHistoryService.addChatMessage(appId, userMessage, ChatHistoryMessageTypeEnum.USER.getValue(),
                                          loginUser.getId());

        // 设置监控上下文
        MonitorContextHolder.setContext(MonitorContext.builder()
                                                      .appId(appId.toString())
                                                      .userId(loginUser.getId()
                                                                       .toString())
                                                      .build());
        // 调用 AI 生成
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);

        // 调用消息执行器
        return streamHandlerExecutor.doExecute(contentFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum)
                                    .doFinally((signalType) -> MonitorContextHolder.clearContext());
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 权限校验
        if (!app.getUserId()
                .equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 检查是否已有 deployKey
        // 如果没有，则生成 6 位 （字母+数字）
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 获取代码生成类型，获取原始代码生成路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        Path sourceDirPath = Path.of(AppConstant.CODE_OUTPUT_ROOT_DIR, sourceDirName);
        // 检查路径是否存在
        if (Files.notExists(sourceDirPath) || !Files.isDirectory(sourceDirPath)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码路径不存在");
        }
        // Vue 项目特殊处理：执行构建
        if (CodeGenTypeEnum.VUE_PROJECT.getValue()
                                       .equals(codeGenType)) {
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath.toString());
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请重试");
            // 检查 dist 是否存在
            File distDir = new File(sourceDirPath.toString(), "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建成功但未生成 dist");
            sourceDirPath = Path.of(distDir.getAbsolutePath());
        }

        // 复制文件到部署目录
        Path deployDirPath = Path.of(AppConstant.CODE_DEPLOY_ROOT_DIR, deployKey);
        try {
            FileUtil.copyContent(sourceDirPath.toFile(), deployDirPath.toFile(), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败: " + e);
        }

        // 更新数据库
        app.setDeployKey(deployKey);
        app.setDeployedTime(LocalDateTime.now());
        boolean res = this.updateById(app);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "更新应用失败");
        // 返回可访问的 URL 地址
        String deployUrl = Path.of(AppConstant.CODE_DEPLOY_HOST, deployKey)
                               .toString();
        // 异步生成截图并更新封面
        generateAppScreenShotAsync(appId, deployUrl);
        return deployUrl;
    }

    /**
     * 异步生成应用程序截图并更新数据库中的封面信息。
     *
     * @param appId  应用程序的唯一标识符，用于标识需要更新封面的应用程序
     * @param appUrl 应用程序对应的网址，用于生成截图
     */
    @Override
    public void generateAppScreenShotAsync(Long appId, String appUrl) {
        Thread.startVirtualThread(() -> {
            // 调用截图并上传
            String screenshotUrl = screenShotService.generateAndUploadScreenshot(appUrl);
            // 更新数据库封面
            App app = mapper.selectOneById(appId);
            app.setCover(screenshotUrl);
            ThrowUtils.throwIf(!this.updateById(app), ErrorCode.OPERATION_ERROR, "更新封面失败" + appId);
        });
    }

    /**
     * 删除应用时，关联删除对应的历史
     *
     * @param id 应用 ID
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(Serializable id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        Long appId = (Long) id;

        try {
            // 先删除关联的对话历史
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用关联的对话历史失败: {}", e.getMessage());
        }
        return super.removeById(id);
    }
}
