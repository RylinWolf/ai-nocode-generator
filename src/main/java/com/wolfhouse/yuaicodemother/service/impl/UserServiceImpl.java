package com.wolfhouse.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wolfhouse.yuaicodemother.common.constant.UserConstant;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.mapper.UserMapper;
import com.wolfhouse.yuaicodemother.model.dto.UserQueryRequest;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.enums.UserRoleEnum;
import com.wolfhouse.yuaicodemother.model.vo.UserLoginVo;
import com.wolfhouse.yuaicodemother.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.wolfhouse.yuaicodemother.model.entity.table.UserTableDef.USER;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/rylinwolf">Rylin</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帐号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不一致");
        }
        // 查询用户是否存在
        if (mapper.selectCountByQuery(QueryWrapper.create()
                                                  .where(USER.USER_ACCOUNT.eq(userAccount))) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }
        // 加密密码
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(getEncryptPassword(userPassword));
        user.setUserRole(UserRoleEnum.USER);
        // 创建用户
        if (mapper.insert(user, true) < 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库插入出错");
        }
        return user.getId();
    }

    @Override
    public UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取用户
        if (mapper.selectCountByQuery(QueryWrapper.create()
                                                  .where(USER.USER_ACCOUNT.eq(userAccount))) < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 加密
        User user = mapper.selectOneByQuery(QueryWrapper.create()
                                                        .where(USER.USER_ACCOUNT.eq(userAccount))
                                                        .and(USER.USER_PASSWORD.eq(getEncryptPassword(userPassword))));
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 记录用户登录态
        request.getSession()
               .setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 返回脱敏后的用户信息
        return BeanUtil.copyProperties(user, UserLoginVo.class);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession()
                                .getAttribute(UserConstant.USER_LOGIN_STATE);
        var login = (User) userObj;
        Long userId;
        if (login == null || (userId = login.getId()) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 查询当前用户信息
        User user = mapper.selectOneByQuery(QueryWrapper.create()
                                                        .where(USER.ID.eq(userId)));
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;

    }

    @Override
    public boolean logout(HttpServletRequest request) {
        // 确保已登录
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 移除登录态
        request.getSession()
               .removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                           .where(USER.ID.eq(id))
                           .and(USER.USER_ROLE.eq(userRole))
                           .and(USER.USER_ACCOUNT.like(userAccount))
                           .and(USER.USER_NAME.like(userName))
                           .and(USER.USER_PROFILE.like(userProfile))
                           .orderBy(sortField, "ascend".equals(sortOrder));
    }


    /**
     * 盐值加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String password) {
        final String salt = "RYLIN";
        return DigestUtils.md5DigestAsHex((password + salt).getBytes());
    }
}
