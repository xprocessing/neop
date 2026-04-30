package com.gongziyu.neop.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.UserInfo;
import com.gongziyu.neop.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserInfoMapper userInfoMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/list")
    public Result<Map<String, Object>> list(PageDTO pageDTO,
                                            @RequestParam(required = false) String username,
                                            @RequestParam(required = false) String nickname,
                                            @RequestParam(required = false) String phone,
                                            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(nickname)) {
            wrapper.like(UserInfo::getNickname, nickname);
        } else if (org.apache.commons.lang3.StringUtils.isNotBlank(username)) {
            wrapper.like(UserInfo::getNickname, username);
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(phone)) {
            wrapper.like(UserInfo::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(UserInfo::getStatus, status);
        }
        wrapper.orderByDesc(UserInfo::getCreateTime);
        IPage<UserInfo> page = userInfoMapper.selectPage(pageDTO.getPage(), wrapper);

        java.util.List<UserInfo> records = page.getRecords();
        java.util.List<Map<String, Object>> list = records.stream().map(u -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getNickname());
            m.put("nickname", u.getNickname());
            m.put("phone", u.getPhone());
            m.put("email", "");
            m.put("gender", u.getSex());
            m.put("avatar", u.getAvatar() != null ? u.getAvatar() : "");
            m.put("points", 0);
            m.put("memberLevel", "");
            m.put("status", u.getStatus());
            m.put("createTime", u.getCreateTime());
            return m;
        }).collect(java.util.stream.Collectors.toList());

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("list", list);
        result.put("total", page.getTotal());
        return Result.success(result);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Map<String, Object> body) {
        UserInfo user = new UserInfo();
        user.setId(((Number) body.get("id")).longValue());
        if (body.containsKey("nickname")) {
            user.setNickname((String) body.get("nickname"));
        }
        if (body.containsKey("phone")) {
            user.setPhone((String) body.get("phone"));
        }
        Object gender = body.get("gender");
        if (gender != null) {
            user.setSex(((Number) gender).intValue());
        }
        Object status = body.get("status");
        if (status != null) {
            user.setStatus(((Number) status).intValue());
        }
        userInfoMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        UserInfo user = new UserInfo();
        user.setNickname((String) body.getOrDefault("nickname", ""));
        user.setPhone((String) body.getOrDefault("phone", ""));
        Object gender = body.get("gender");
        if (gender != null) {
            user.setSex(((Number) gender).intValue());
        }
        user.setStatus(1);
        userInfoMapper.insert(user);
        return Result.success();
    }

    @PutMapping("/reset-password/{id}")
    public Result<Void> resetPassword(@PathVariable Long id) {
        return Result.success();
    }
}
