package com.wolfhouse.yuaicodeuser.service.Impl;

import com.wolfhouse.yuaicodemother.innerservice.InnerUserService;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.vo.UserVO;
import com.wolfhouse.yuaicodeuser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Rylin Wolf
 */
@Component
@RequiredArgsConstructor
@DubboService
public class InnerUserServiceImpl implements InnerUserService {
    private final UserService userService;

    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }

    @Override
    public UserVO getUserVO(User user) {
        return userService.getUserVo(user);
    }
}
