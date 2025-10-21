package com.wolfhouse.yuaicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.wolfhouse.yuaicodemother.common.BaseResponse;
import com.wolfhouse.yuaicodemother.common.DeleteRequest;
import com.wolfhouse.yuaicodemother.common.ResultUtils;
import com.wolfhouse.yuaicodemother.common.annotation.AuthCheck;
import com.wolfhouse.yuaicodemother.common.constant.UserConstant;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.exception.ThrowUtils;
import com.wolfhouse.yuaicodemother.model.dto.user.*;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.vo.UserLoginVo;
import com.wolfhouse.yuaicodemother.model.vo.UserVO;
import com.wolfhouse.yuaicodemother.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author <a href="https://github.com/rylinwolf">Rylin</a>
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();
        long count = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(count);
    }

    @PostMapping("/login")
    public BaseResponse<UserLoginVo> userLogin(@RequestBody UserLoginRequest request,
                                               HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        UserLoginVo userLoginVo = userService.userLogin(request.getUserAccount(),
                                                        request.getUserPassword(),
                                                        httpServletRequest);
        return ResultUtils.success(userLoginVo);
    }

    @GetMapping("/get/login")
    public BaseResponse<UserLoginVo> getLoginUser(HttpServletRequest httpServletRequest) {
        return ResultUtils.success(BeanUtil.copyProperties(userService.getLoginUser(httpServletRequest),
                                                           UserLoginVo.class));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.logout(httpServletRequest));
    }

    /**
     * 保存用户。
     *
     * @param user 用户
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 根据主键删除用户。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return userService.removeById(id);
    }

    /**
     * 根据主键更新用户。
     *
     * @param user 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    /**
     * 查询所有用户。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<User> list() {
        return userService.list();
    }

    /**
     * 根据主键获取用户。
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @GetMapping("getInfo/{id}")
    public User getInfo(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 分页查询用户。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<User> page(Page<User> page) {
        return userService.page(page);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String defaultPassword = "12345678";
        String encryptPassword = userService.getEncryptPassword(defaultPassword);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVoById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(BeanUtil.copyProperties(user, UserVO.class));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(hasRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                                               userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVoPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVoList = BeanUtil.copyToList(userPage.getRecords(), UserVO.class);
        userVoPage.setRecords(userVoList);
        return ResultUtils.success(userVoPage);
    }
}
