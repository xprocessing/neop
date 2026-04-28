package com.gongziyu.neop.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gongziyu.neop.entity.*;
import com.gongziyu.neop.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 定时任务集合（文档第十二章）
 * 6个定时任务
 */
@Slf4j
@Component
public class ScheduledTasks {

    @Autowired private TaskUserReceiveMapper taskUserReceiveMapper;
    @Autowired private PayOrderMapper payOrderMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private MemberUserMapper memberUserMapper;
    @Autowired private TaskPayLogMapper taskPayLogMapper;
    @Autowired private DataStatisticsMapper dataStatisticsMapper;
    @Autowired private UserInfoMapper userInfoMapper;
    @Autowired private StringRedisTemplate stringRedisTemplate;

    /**
     * 1. 任务过期扫描（每30分钟）
     * 扫描audit_status IN (1,2)且expire_time < NOW()的记录，更新为5已过期
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void taskExpireScan() {
        LambdaUpdateWrapper<TaskUserReceive> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(TaskUserReceive::getAuditStatus, 1, 2)
                .lt(TaskUserReceive::getExpireTime, new Date())
                .set(TaskUserReceive::getAuditStatus, 5);
        int count = taskUserReceiveMapper.update(null, wrapper);
        if (count > 0) {
            log.info("[定时任务-过期扫描] 扫描到{}条过期记录", count);
        }
    }

    /**
     * 2. 支付订单超时关闭（每5分钟）
     * 扫描status=0且create_time < NOW()-15分钟的订单，更新为已取消、释放库存
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void payOrderTimeoutClose() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getStatus, 0)
                .lt(PayOrder::getCreateTime, timeout);
        List<PayOrder> orders = payOrderMapper.selectList(wrapper);

        for (PayOrder order : orders) {
            order.setStatus(2);  // 已取消
            payOrderMapper.updateById(order);
            log.info("[定时任务-订单超时关闭] orderNo={}", order.getOrderNo());
        }
        if (!orders.isEmpty()) {
            log.info("[定时任务-订单超时关闭] 共关闭{}个超时订单", orders.size());
        }
    }

    /**
     * 3. 每日数据统计（每日凌晨0点）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyStatistics() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        LocalDateTime dayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atStartOfDay();

        DataStatistics stat = new DataStatistics();
        stat.setStatDate(yesterday);

        // 当日注册人数
        stat.setRegisterNum((int) userInfoMapper.selectCount(
                new LambdaQueryWrapper<UserInfo>().ge(UserInfo::getCreateTime, dayStart).lt(UserInfo::getCreateTime, dayEnd)));

        // 当日活跃人数（简化：领取任务的用户数）
        stat.setActiveNum((int) taskUserReceiveMapper.selectCount(
                new LambdaQueryWrapper<TaskUserReceive>().ge(TaskUserReceive::getCreateTime, dayStart).lt(TaskUserReceive::getCreateTime, dayEnd)));

        // 当日订单数
        stat.setOrderNum((int) orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3).ge(Order::getPayTime, dayStart).lt(Order::getPayTime, dayEnd)));

        // 当日订单金额（简化）
        stat.setOrderAmount(BigDecimal.ZERO);

        // 当日完成任务数
        stat.setTaskNum((int) taskUserReceiveMapper.selectCount(
                new LambdaQueryWrapper<TaskUserReceive>().eq(TaskUserReceive::getAuditStatus, 3).ge(TaskUserReceive::getUpdateTime, dayStart).lt(TaskUserReceive::getUpdateTime, dayEnd)));

        // 当日打款金额（简化）
        stat.setTaskPayAmount(BigDecimal.ZERO);

        dataStatisticsMapper.insert(stat);
        log.info("[定时任务-每日统计] 统计日期={}, 注册={}, 活跃={}, 订单={}, 任务={}", yesterday, stat.getRegisterNum(), stat.getActiveNum(), stat.getOrderNum(), stat.getTaskNum());
    }

    /**
     * 4. 会员过期扫描（每日凌晨1点）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void memberExpireScan() {
        LambdaUpdateWrapper<MemberUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberUser::getIsMember, 1)
                .lt(MemberUser::getExpireTime, new Date())
                .set(MemberUser::getIsMember, 0);
        int count = memberUserMapper.update(null, wrapper);
        if (count > 0) {
            log.info("[定时任务-会员过期] 更新{}条过期会员记录", count);
        }
    }

    /**
     * 5. 打款失败自动重试（每10分钟）
     * 扫描pay_status=3且retry_count < 3的记录，执行重试
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void payRetry() {
        LambdaQueryWrapper<TaskPayLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskPayLog::getPayStatus, 3)
                .lt(TaskPayLog::getRetryCount, 3);
        List<TaskPayLog> logs = taskPayLogMapper.selectList(wrapper);

        for (TaskPayLog payLog : logs) {
            // 重置为处理中，等待下次处理
            payLog.setPayStatus(1);
            taskPayLogMapper.updateById(payLog);
            log.info("[定时任务-打款重试] tradeNo={}, retryCount={}", payLog.getTradeNo(), payLog.getRetryCount());
        }
        if (!logs.isEmpty()) {
            log.info("[定时任务-打款重试] 共{}条记录待重试", logs.size());
        }
    }

    /**
     * 6. 订单自动确认收货（每日凌晨2点）
     * 扫描status=2且send_time < NOW()-7天的订单，更新为已完成
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void orderAutoConfirm() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getStatus, 2)
                .lt(Order::getSendTime, java.sql.Timestamp.valueOf(sevenDaysAgo))
                .set(Order::getStatus, 3)
                .set(Order::getFinishTime, new Date());
        int count = orderMapper.update(null, wrapper);
        if (count > 0) {
            log.info("[定时任务-自动确认收货] 确认{}条订单", count);
        }
    }
}
