package com.wolfhouse.yuaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wolfhouse.yuaicodemother.model.dto.UserQueryRequest;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.vo.UserLoginVo;
import com.wolfhouse.yuaicodemother.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/rylinwolf">Rylin</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录方法。
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求对象
     * @return 返回用户登录信息对象 UserLoginVo，如果登录失败可能返回空值或抛出异常
     */
    UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户的信息。
     *
     * @param request 请求对象，用于获取当前用户的会话信息
     * @return 返回当前登录的用户信息，如果未登录可能返回空值或抛出异常
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 执行用户登出操作。
     *
     * @param request 请求对象，用于获取用户的会话信息
     * @return 如果登出操作成功返回 true，否则返回 false
     */
    boolean logout(HttpServletRequest request);

    QueryWrapper getQueryWrapper(UserQueryRequest request);

    String getEncryptPassword(String password);

    UserVO getUserVo(User user);
}
