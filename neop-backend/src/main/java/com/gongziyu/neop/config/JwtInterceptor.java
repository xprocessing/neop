package com.gongziyu.neop.config;

import com.gongziyu.neop.exception.BusinessException;
import com.gongziyu.neop.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * JWT Token校验拦截器（文档28.2节）
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token) || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或Token失效");
        }
        token = token.substring(7);

        // 解析Token
        Claims claims = jwtUtil.parseToken(token);
        String type = claims.get("type", String.class);

        // 检查Redis黑名单
        String blacklistKey;
        if ("admin".equals(type)) {
            Long adminId = claims.get("adminId", Long.class);
            blacklistKey = "neop:token:blacklist:admin:" + adminId;
        } else {
            Long userId = claims.get("userId", Long.class);
            blacklistKey = "neop:token:blacklist:mobile:" + userId;
        }

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey))) {
            throw new BusinessException(401, "Token已失效，请重新登录");
        }

        // 将用户信息存入请求上下文
        request.setAttribute("claims", claims);
        if ("admin".equals(type)) {
            request.setAttribute("adminId", claims.get("adminId", Long.class));
            request.setAttribute("username", claims.get("username", String.class));
        } else {
            request.setAttribute("userId", claims.get("userId", Long.class));
        }
        request.setAttribute("userType", type);

        return true;
    }

    /**
     * 将Token加入黑名单（登出/踢人时调用）
     */
    public void addBlacklist(Long userId, String type, String token) {
        String blacklistKey;
        if ("admin".equals(type)) {
            blacklistKey = "neop:token:blacklist:admin:" + userId;
        } else {
            blacklistKey = "neop:token:blacklist:mobile:" + userId;
        }
        long remainingTime = jwtUtil.getTokenRemainingTime(token);
        if (remainingTime > 0) {
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", remainingTime, TimeUnit.MILLISECONDS);
        }
    }
}
