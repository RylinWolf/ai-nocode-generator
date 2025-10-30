package com.wolfhouse.yuaicodeuser.aop;

import com.wolfhouse.yuaicodemother.annotation.AuthCheck;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.model.entity.User;
import com.wolfhouse.yuaicodemother.model.enums.UserRoleEnum;
import com.wolfhouse.yuaicodeuser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author linexsong
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuthInterceptor {
    private final UserService userService;

    /**
     * 拦截方法，检查注解标注的方法是否符合权限要求。
     *
     * @param joinPoint 方法切入点，包含被拦截方法的相关信息
     * @param authCheck 通过注解传入的权限检查参数，包括所需的角色信息
     * @return 方法执行结果的返回值，如果权限校验失败可能抛出异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String role = authCheck.hasRole();
        // 获取当前登录用户
        ServletRequestAttributes servletRequestAttributes =
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        User loginUser = userService.getLoginUser(servletRequestAttributes.getRequest());
        UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(role);
        // 无需权限
        if (roleEnum == null) {
            return joinPoint.proceed();
        }
        UserRoleEnum userRole = loginUser.getUserRole();
        // 无权限
        if (userRole == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 没有管理员权限
        if (UserRoleEnum.ADMIN.equals(roleEnum) && !UserRoleEnum.ADMIN.equals(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过
        return joinPoint.proceed();
    }
}
