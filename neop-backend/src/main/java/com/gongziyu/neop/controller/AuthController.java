package com.gongziyu.neop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.UserInfo;
import com.gongziyu.neop.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 前台认证接口（手机号+密码）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 发送短信验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        // In real app, send SMS via service. For now, always succeed.
        return Result.success();
    }

    /**
     * 发送绑定验证码
     */
    @PostMapping("/send-bind-code")
    public Result<Void> sendBindCode(@RequestBody Map<String, String> body) {
        return Result.success();
    }

    /**
     * 发送重置密码验证码
     */
    @PostMapping("/send-reset-code")
    public Result<Void> sendResetCode(@RequestBody Map<String, String> body) {
        return Result.success();
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        String phone = (String) body.get("phone");
        String password = (String) body.get("password");
        String inviteCode = (String) body.getOrDefault("inviteCode", "");

        // Check if phone already exists
        UserInfo existing = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone));
        if (existing != null) {
            return Result.error(400, "该手机号已注册");
        }

        UserInfo user = new UserInfo();
        user.setPhone(phone);
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setStatus(1);
        userInfoMapper.insert(user);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("token", "mock-token-" + user.getId());
        return Result.success(result);
    }

    /**
     * 手机号+密码登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");

        UserInfo user = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone));
        if (user == null) {
            return Result.error(400, "用户不存在");
        }
        if (user.getPassword() != null
                && !new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            return Result.error(400, "密码错误");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("token", "mock-token-" + user.getId());
        result.put("refreshToken", "mock-refresh-" + user.getId());
        return Result.success(result);
    }

    /**
     * 验证手机号
     */
    @PostMapping("/verify-phone")
    public Result<Void> verifyPhone(@RequestBody Map<String, String> body) {
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, Object> body) {
        String phone = (String) body.get("phone");
        String password = (String) body.get("password");

        UserInfo user = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone));
        if (user != null) {
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            userInfoMapper.updateById(user);
        }
        return Result.success();
    }
}
