package com.gongziyu.neop.controller.sys;

import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台数据看板接口（文档8.6.2节）
 */
@RestController
@RequestMapping("/api/admin")
public class DashboardController {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TaskUserReceiveMapper taskUserReceiveMapper;

    @Autowired
    private TaskPayLogMapper taskPayLogMapper;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 总用户数
        data.put("totalUsers", userInfoMapper.selectCount(null));

        // 今日新增用户
        data.put("todayNewUsers", userInfoMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo>()
                        .ge("create_time", todayStart)));

        // 今日活跃用户（简化：今日登录/领取任务/提交任务）
        data.put("todayActiveUsers", taskUserReceiveMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TaskUserReceive>()
                        .ge("create_time", todayStart)));

        // 今日订单金额
        data.put("todayOrderAmount", 0);

        // 今日打款金额
        data.put("todayPayAmount", 0);

        // 待审核任务数
        data.put("pendingAuditCount", taskUserReceiveMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TaskUserReceive>()
                        .eq("audit_status", 2)));

        // 待打款数量
        data.put("pendingPayCount", taskUserReceiveMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TaskUserReceive>()
                        .eq("audit_status", 3)
                        .eq("withdraw_status", 0)));

        return Result.success(data);
    }
}
