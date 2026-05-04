package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.*;
import com.gongziyu.neop.exception.BusinessException;
import com.gongziyu.neop.exception.LockAcquisitionException;
import com.gongziyu.neop.mapper.*;
import com.gongziyu.neop.service.PointService;
import com.gongziyu.neop.service.TaskService;
import com.gongziyu.neop.util.OrderNoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TaskServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo> implements TaskService {

    @Autowired private TaskInfoMapper taskInfoMapper;
    @Autowired private TaskUserReceiveMapper taskUserReceiveMapper;
    @Autowired private TaskSubmitMapper taskSubmitMapper;
    @Autowired private TaskPayLogMapper taskPayLogMapper;
    @Autowired private UserWechatMapper userWechatMapper;
    @Autowired private RedissonClient redissonClient;
    @Autowired private StringRedisTemplate stringRedisTemplate;
    @Autowired private PointService pointService;

    // ===== 前台接口 =====

    @Override
    public IPage<Map<String, Object>> frontList(PageDTO pageDTO, String keyword) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskInfo::getStatus, 1)
               .like(keyword != null && !keyword.isEmpty(), TaskInfo::getTaskTitle, keyword)
               .orderByDesc(TaskInfo::getSort, TaskInfo::getCreateTime);
        IPage<TaskInfo> taskPage = taskInfoMapper.selectPage(pageDTO.getPage(), wrapper);
        return taskPage.convert(this::buildTaskMap);
    }

    @Override
    public Map<String, Object> frontInfo(Long taskId) {
        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null || task.getStatus() == 0) {
            throw BusinessException.of(1001, "任务不存在或已下架");
        }
        return buildTaskMap(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public Map<String, Object> receiveTask(Long userId, Long taskId) {
        // 分布式锁防重复领取
        String lockKey = "neop:lock:task:receive:" + taskId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new LockAcquisitionException("操作过于频繁");
            }

            // 1. 校验任务
            TaskInfo task = taskInfoMapper.selectById(taskId);
            if (task == null || task.getStatus() == 0) {
                throw BusinessException.of(1001, "任务不存在或已下架");
            }

            // 2. 校验今日领取次数
            String today = LocalDateTime.now().toLocalDate().toString();
            String countKey = "neop:task:receive:count:userId:" + userId + ":" + taskId + ":" + today;
            Long todayCount = stringRedisTemplate.opsForValue().increment(countKey);
            if (todayCount != null && todayCount == 1) {
                // 首次计数，设置过期时间到当日23:59:59
                long secondsUntilMidnight = LocalDateTime.now().until(
                        LocalDateTime.now().plusDays(1).with(LocalTime.MIN), java.time.temporal.ChronoUnit.SECONDS);
                stringRedisTemplate.expire(countKey, secondsUntilMidnight, TimeUnit.SECONDS);
            }
            if (todayCount != null && todayCount > task.getDayNum()) {
                throw BusinessException.of(1002, "今日任务领取次数已达上限");
            }

            // 3. 校验是否存在未过期同任务工单
            LambdaQueryWrapper<TaskUserReceive> receiveWrapper = new LambdaQueryWrapper<>();
            receiveWrapper.eq(TaskUserReceive::getUserId, userId)
                    .eq(TaskUserReceive::getTaskId, taskId)
                    .in(TaskUserReceive::getAuditStatus, 1, 2, 3, 4);
            long existCount = taskUserReceiveMapper.selectCount(receiveWrapper);
            if (existCount > 0) {
                throw BusinessException.of(1003, "您已领取过该任务，请勿重复领取");
            }

            // 4. 校验剩余可领取数量
            LambdaQueryWrapper<TaskUserReceive> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(TaskUserReceive::getTaskId, taskId)
                    .notIn(TaskUserReceive::getAuditStatus, 5);  // 排除已过期
            long usedCount = taskUserReceiveMapper.selectCount(stockWrapper);
            if (usedCount >= task.getTotalNum()) {
                throw BusinessException.of(1001, "任务名额已满");
            }

            // 5. 获取用户openid
            LambdaQueryWrapper<UserWechat> wechatWrapper = new LambdaQueryWrapper<>();
            wechatWrapper.eq(UserWechat::getUserId, userId);
            UserWechat userWechat = userWechatMapper.selectOne(wechatWrapper);

            // 6. 创建领取记录
            TaskUserReceive receive = new TaskUserReceive();
            receive.setUserId(userId);
            receive.setTaskId(taskId);
            receive.setOpenid(userWechat != null ? userWechat.getOpenid() : "");
            receive.setRewardAmount(task.getRewardAmount());
            receive.setAuditStatus(1);  // 待提交
            receive.setGrantPay(0);
            receive.setWithdrawStatus(0);
            // 计算过期时间
            LocalDateTime expireTime = LocalDateTime.now().plusMinutes(task.getExpireMinute());
            receive.setExpireTime(Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()));
            taskUserReceiveMapper.insert(receive);

            log.info("[任务领取] userId={}, taskId={}, receiveId={}", userId, taskId, receive.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("receiveId", receive.getId());
            result.put("expireTime", receive.getExpireTime());
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BusinessException.of("领取任务失败");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTask(Long userId, Long receiveId, String submitImages, String submitNote) {
        TaskUserReceive receive = taskUserReceiveMapper.selectById(receiveId);
        if (receive == null || !receive.getUserId().equals(userId)) {
            throw BusinessException.of(1001, "任务领取记录不存在");
        }
        if (receive.getAuditStatus() != 1 && receive.getAuditStatus() != 4) {
            throw BusinessException.of(1005, "当前任务状态不支持提交");
        }
        // 检查是否过期
        if (receive.getExpireTime() != null && receive.getExpireTime().before(new Date())) {
            throw BusinessException.of(1004, "任务已过期，无法操作");
        }

        // 创建提交工单
        TaskSubmit submit = new TaskSubmit();
        submit.setReceiveId(receiveId);
        submit.setUserId(userId);
        submit.setSubmitImages(submitImages);
        submit.setSubmitNote(submitNote);
        taskSubmitMapper.insert(submit);

        // 更新领取记录状态为待审核
        receive.setAuditStatus(2);
        taskUserReceiveMapper.updateById(receive);

        log.info("[任务提交] userId={}, receiveId={}", userId, receiveId);
    }

    @Override
    public IPage<Map<String, Object>> myTaskList(Long userId, PageDTO pageDTO, Integer auditStatus) {
        LambdaQueryWrapper<TaskUserReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskUserReceive::getUserId, userId);
        if (auditStatus != null) {
            wrapper.eq(TaskUserReceive::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(TaskUserReceive::getCreateTime);
        IPage<TaskUserReceive> receivePage = taskUserReceiveMapper.selectPage(pageDTO.getPage(), wrapper);
        return receivePage.convert(receive -> {
            Map<String, Object> map = new HashMap<>();
            map.put("receive", receive);
            TaskInfo task = taskInfoMapper.selectById(receive.getTaskId());
            map.put("task", task);
            return map;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> withdrawApply(Long userId, Long receiveId) {
        TaskUserReceive receive = taskUserReceiveMapper.selectById(receiveId);
        if (receive == null || !receive.getUserId().equals(userId)) {
            throw BusinessException.of(1001, "任务领取记录不存在");
        }

        // 提现三校验（硬性规则）
        if (receive.getAuditStatus() != 3) {
            throw BusinessException.of(1006, "任务未审核通过，无法申请提现");
        }
        if (receive.getWithdrawStatus() != 0) {
            throw BusinessException.of(1007, "该任务已完成打款，请勿重复操作");
        }
        if (receive.getRewardAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw BusinessException.of(1006, "奖励金额无效");
        }

        // 幂等校验
        String idempotentKey = "neop:wechat:callback:idempotent:withdraw:" + receiveId;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(idempotentKey))) {
            throw BusinessException.of(1007, "请勿重复申请提现");
        }

        // 生成交易单号
        String tradeNo = OrderNoUtil.generateTradeNo();

        // 创建打款日志
        TaskInfo task = taskInfoMapper.selectById(receive.getTaskId());
        TaskPayLog payLog = new TaskPayLog();
        payLog.setReceiveId(receiveId);
        payLog.setUserId(userId);
        payLog.setTaskTitle(task != null ? task.getTaskTitle() : "");
        payLog.setTradeNo(tradeNo);
        payLog.setPayAmount(receive.getRewardAmount());
        payLog.setPayStatus(1);  // 处理中
        payLog.setRetryCount(0);
        payLog.setApplyTime(new Date());
        taskPayLogMapper.insert(payLog);

        // 更新提现状态
        receive.setWithdrawStatus(1);  // 处理中
        taskUserReceiveMapper.updateById(receive);

        // 设置幂等标记
        stringRedisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);

        log.info("[提现申请] userId={}, receiveId={}, tradeNo={}, amount={}", userId, receiveId, tradeNo, receive.getRewardAmount());

        Map<String, Object> result = new HashMap<>();
        result.put("tradeNo", tradeNo);
        result.put("amount", receive.getRewardAmount());
        return result;
    }

    @Override
    public IPage<TaskPayLog> withdrawLog(Long userId, PageDTO pageDTO) {
        LambdaQueryWrapper<TaskPayLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskPayLog::getUserId, userId).orderByDesc(TaskPayLog::getCreateTime);
        return taskPayLogMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    // ===== 后台接口 =====

    @Override
    public IPage<TaskInfo> adminList(PageDTO pageDTO, String taskTitle, Integer status) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(taskTitle)) {
            wrapper.like(TaskInfo::getTaskTitle, taskTitle);
        }
        if (status != null) {
            wrapper.eq(TaskInfo::getStatus, status);
        }
        wrapper.orderByDesc(TaskInfo::getCreateTime);
        return taskInfoMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTask(TaskInfo taskInfo) {
        taskInfoMapper.insert(taskInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(TaskInfo taskInfo) {
        taskInfoMapper.updateById(taskInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        taskInfoMapper.deleteById(id);
    }

    @Override
    public void updateTaskStatus(Long id, Integer status) {
        TaskInfo task = new TaskInfo();
        task.setId(id);
        task.setStatus(status);
        taskInfoMapper.updateById(task);
    }

    @Override
    public IPage<Map<String, Object>> auditList(PageDTO pageDTO, Integer auditStatus) {
        LambdaQueryWrapper<TaskUserReceive> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(TaskUserReceive::getAuditStatus, auditStatus);
        } else {
            wrapper.eq(TaskUserReceive::getAuditStatus, 2);  // 默认查待审核
        }
        wrapper.orderByAsc(TaskUserReceive::getCreateTime);
        IPage<TaskUserReceive> receivePage = taskUserReceiveMapper.selectPage(pageDTO.getPage(), wrapper);
        return receivePage.convert(receive -> {
            Map<String, Object> map = new HashMap<>();
            map.put("receive", receive);
            map.put("task", taskInfoMapper.selectById(receive.getTaskId()));
            LambdaQueryWrapper<TaskSubmit> submitWrapper = new LambdaQueryWrapper<>();
            submitWrapper.eq(TaskSubmit::getReceiveId, receive.getId());
            map.put("submit", taskSubmitMapper.selectOne(submitWrapper));
            return map;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void auditPass(Long receiveId, Long adminId) {
        // 加行锁防并发审核
        TaskUserReceive receive = taskUserReceiveMapper.selectForUpdate(receiveId);
        if (receive == null) {
            throw BusinessException.of(1001, "任务领取记录不存在");
        }
        if (receive.getAuditStatus() != 2) {
            throw BusinessException.of(1005, "当前任务状态不支持审核通过");
        }

        // 更新审核状态
        receive.setAuditStatus(3);
        taskUserReceiveMapper.updateById(receive);

        log.info("[审核通过] receiveId={}, adminId={}", receiveId, adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReject(Long receiveId, Long adminId, String auditNote) {
        TaskUserReceive receive = taskUserReceiveMapper.selectById(receiveId);
        if (receive == null) {
            throw BusinessException.of(1001, "任务领取记录不存在");
        }
        if (receive.getAuditStatus() != 2) {
            throw BusinessException.of(1005, "当前任务状态不支持审核驳回");
        }

        receive.setAuditStatus(4);
        taskUserReceiveMapper.updateById(receive);

        // 更新提交工单的审核备注
        LambdaQueryWrapper<TaskSubmit> submitWrapper = new LambdaQueryWrapper<>();
        submitWrapper.eq(TaskSubmit::getReceiveId, receiveId);
        TaskSubmit submit = taskSubmitMapper.selectOne(submitWrapper);
        if (submit != null) {
            submit.setAuditNote(auditNote);
            submit.setAuditTime(new Date());
            taskSubmitMapper.updateById(submit);
        }

        log.info("[审核驳回] receiveId={}, adminId={}, auditNote={}", receiveId, adminId, auditNote);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPay(Long receiveId, Long adminId) {
        TaskUserReceive receive = taskUserReceiveMapper.selectById(receiveId);
        if (receive == null) {
            throw BusinessException.of(1001, "任务领取记录不存在");
        }
        if (receive.getAuditStatus() != 3) {
            throw BusinessException.of(1006, "任务未审核通过，无法打款");
        }
        if (receive.getWithdrawStatus() == 2) {
            throw BusinessException.of(1007, "该任务已完成打款");
        }

        // 设置授权打款标记
        receive.setGrantPay(1);
        taskUserReceiveMapper.updateById(receive);

        // TODO: 调用微信企业付款接口（阶段6实现）
        log.info("[授权打款] receiveId={}, adminId={}, amount={}", receiveId, adminId, receive.getRewardAmount());
    }

    @Override
    public IPage<TaskPayLog> payLogList(PageDTO pageDTO, Integer payStatus) {
        LambdaQueryWrapper<TaskPayLog> wrapper = new LambdaQueryWrapper<>();
        if (payStatus != null) {
            wrapper.eq(TaskPayLog::getPayStatus, payStatus);
        }
        wrapper.orderByDesc(TaskPayLog::getCreateTime);
        return taskPayLogMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    private Map<String, Object> buildTaskMap(TaskInfo task) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("taskTitle", task.getTaskTitle());
        map.put("taskCover", task.getTaskCover());
        map.put("taskIntro", task.getTaskIntro());
        map.put("rewardAmount", task.getRewardAmount());
        map.put("dayNum", task.getDayNum());
        map.put("totalNum", task.getTotalNum());
        map.put("expireMinute", task.getExpireMinute());
        map.put("status", task.getStatus());
        map.put("sort", task.getSort());

        // 计算剩余可领取数量
        LambdaQueryWrapper<TaskUserReceive> stockWrapper = new LambdaQueryWrapper<>();
        stockWrapper.eq(TaskUserReceive::getTaskId, task.getId())
                .notIn(TaskUserReceive::getAuditStatus, 5);
        long usedCount = taskUserReceiveMapper.selectCount(stockWrapper);
        map.put("remainNum", Math.max(0, task.getTotalNum() - usedCount));

        return map;
    }
}
