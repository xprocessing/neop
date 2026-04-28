package com.gongziyu.neop.config;

import com.gongziyu.neop.annotation.RateLimit;
import com.gongziyu.neop.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流AOP切面（文档8.1.3节）
 * 使用Redis滑动窗口实现接口限流
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(com.gongziyu.neop.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 构建限流Key
        String userId = request.getAttribute("userId") != null ? request.getAttribute("userId").toString() : request.getRemoteAddr();
        String key = "neop:rate:limit:" + rateLimit.key() + ":" + userId;

        // 滑动窗口计数
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, rateLimit.period(), TimeUnit.SECONDS);
        }

        if (count != null && count > rateLimit.limit()) {
            log.warn("[限流] key={}, count={}, limit={}", key, count, rateLimit.limit());
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }
}
