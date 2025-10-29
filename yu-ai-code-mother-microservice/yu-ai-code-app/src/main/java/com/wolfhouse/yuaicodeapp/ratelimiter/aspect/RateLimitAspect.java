package com.wolfhouse.yuaicodeapp.ratelimiter.aspect;

import com.wolfhouse.yuaicodeapp.ratelimiter.anno.RateLimit;
import com.wolfhouse.yuaicodemother.exception.BusinessException;
import com.wolfhouse.yuaicodemother.exception.ErrorCode;
import com.wolfhouse.yuaicodemother.innerservice.InnerUserService;
import com.wolfhouse.yuaicodemother.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 限流器切面
 *
 * @author Rylin Wolf
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RedissonClient redissonClient;

    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint joinPoint, RateLimit rateLimit) {
        String key = generateRateLimitKey(joinPoint, rateLimit);
        // 使用 Redisson 的分布式限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 1 小时过期
        rateLimiter.expire(Duration.ofHours(1));
        // 设置限流器参数，每个时间窗口允许的请求数和时间窗口
        rateLimiter.trySetRate(RateType.OVERALL, 5, Duration.ofSeconds(rateLimit.rateInterval()));
        // 尝试获取令牌，获取失败则线流
        if (!rateLimiter.tryAcquire(1)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
        }

    }

    private String generateRateLimitKey(JoinPoint point, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:");
        // 添加自定义前缀
        if (!rateLimit.key()
                      .isEmpty()) {
            keyBuilder.append(rateLimit.key())
                      .append(":");
        }
        // 根据限流类型生成不同的key
        switch (rateLimit.limitType()) {
            case API:
                // 接口级别：方法名
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keyBuilder.append("api:")
                          .append(method.getDeclaringClass()
                                        .getSimpleName())
                          .append(".")
                          .append(method.getName());
                break;
            case USER:
                // 用户级别：用户ID
                try {
                    ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        User loginUser = InnerUserService.getLoginUser(request);
                        keyBuilder.append("user:")
                                  .append(loginUser.getId());
                    } else {
                        // 无法获取请求上下文，使用IP限流
                        keyBuilder.append("ip:")
                                  .append(getClientIp());
                    }
                } catch (BusinessException e) {
                    // 未登录用户使用IP限流
                    keyBuilder.append("ip:")
                              .append(getClientIp());
                }
                break;
            case IP:
                // IP级别：客户端IP
                keyBuilder.append("ip:")
                          .append(getClientIp());
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }


}
