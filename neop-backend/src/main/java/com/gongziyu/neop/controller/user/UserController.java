package com.gongziyu.neop.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.UserInfo;
import com.gongziyu.neop.service.PointService;
import com.gongziyu.neop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 前台用户接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PointService pointService;

    /**
     * 微信登录
     */
    @GetMapping("/wechat/login")
    public Result<Map<String, Object>> wechatLogin(@RequestParam String code,
                                                    @RequestParam(required = false) String inviteCode) {
        Map<String, Object> result = userService.wechatLogin(code, inviteCode);
        return Result.success(result);
    }

    /**
     * 获取用户个人信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = userService.getUserInfo(userId);
        return Result.success(result);
    }

    /**
     * 积分签到
     */
    @PostMapping("/point/sign")
    public Result<Map<String, Object>> sign(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer point = pointService.sign(userId);

        Map<String, Object> data = Map.of(
                "point", point,
                "msg", "签到成功"
        );
        return Result.success(data);
    }

    /**
     * 积分流水记录
     */
    @GetMapping("/point/log")
    public Result<IPage<Map<String, Object>>> pointLog(PageDTO pageDTO,
                                                        @RequestParam(required = false) Integer type,
                                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 通过PointLog Mapper查询分页
        IPage<Map<String, Object>> page = pointService.getBaseMapper()
                .selectMapsPage(pageDTO.getPage(),
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>()
                                .eq("user_id", userId)
                                .eq(type != null, "type", type)
                                .orderByDesc("create_time"));
        return Result.success(page);
    }

    /**
     * 我的邀请记录
     */
    @GetMapping("/invite/log")
    public Result<Map<String, Object>> inviteLog(PageDTO pageDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // TODO: 完善邀请记录查询
        return Result.success(Map.of("records", java.util.Collections.emptyList(), "total", 0));
    }

    /**
     * 后台-用户管理列表
     */
    @GetMapping("/admin/list")
    public Result<IPage<UserInfo>> adminList(PageDTO pageDTO,
                                              @RequestParam(required = false) String nickname,
                                              @RequestParam(required = false) String phone,
                                              @RequestParam(required = false) Integer memberStatus) {
        IPage<UserInfo> page = userService.listPage(pageDTO, nickname, phone, memberStatus);
        return Result.success(page);
    }
}
