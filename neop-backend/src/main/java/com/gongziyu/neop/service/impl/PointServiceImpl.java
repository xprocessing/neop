package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongziyu.neop.common.Constants;
import com.gongziyu.neop.entity.PointLog;
import com.gongziyu.neop.entity.PointUser;
import com.gongziyu.neop.exception.BusinessException;
import com.gongziyu.neop.mapper.PointLogMapper;
import com.gongziyu.neop.mapper.PointUserMapper;
import com.gongziyu.neop.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Slf4j
@Service
public class PointServiceImpl extends ServiceImpl<PointUserMapper, PointUser> implements PointService {

    @Autowired
    private PointUserMapper pointUserMapper;

    @Autowired
    private PointLogMapper pointLogMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoint(Long userId, Integer point, String source, String remark) {
        // 1. 查询积分账户
        LambdaQueryWrapper<PointUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointUser::getUserId, userId);
        PointUser pointUser = pointUserMapper.selectOne(wrapper);

        if (pointUser == null) {
            // 初始化积分账户
            pointUser = new PointUser();
            pointUser.setUserId(userId);
            pointUser.setTotalPoint(point);
            pointUser.setUsablePoint(point);
            pointUserMapper.insert(pointUser);
        } else {
            pointUser.setTotalPoint(pointUser.getTotalPoint() + point);
            pointUser.setUsablePoint(pointUser.getUsablePoint() + point);
            pointUserMapper.updateById(pointUser);
        }

        // 2. 记录积分流水
        PointLog pointLog = new PointLog();
        pointLog.setUserId(userId);
        pointLog.setPoint(point);
        pointLog.setBalance(pointUser.getUsablePoint());
        pointLog.setType(1);  // 1=获取
        pointLog.setSource(source);
        pointLog.setRemark(remark);
        pointLogMapper.insert(pointLog);

        log.info("[积分增加] userId={}, point={}, source={}, balance={}", userId, point, source, pointUser.getUsablePoint());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoint(Long userId, Integer point, String source, String remark) {
        LambdaQueryWrapper<PointUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointUser::getUserId, userId);
        PointUser pointUser = pointUserMapper.selectOne(wrapper);

        if (pointUser == null || pointUser.getUsablePoint() < point) {
            throw BusinessException.of(400, "积分不足");
        }

        pointUser.setUsablePoint(pointUser.getUsablePoint() - point);
        pointUserMapper.updateById(pointUser);

        PointLog pointLog = new PointLog();
        pointLog.setUserId(userId);
        pointLog.setPoint(point);
        pointLog.setBalance(pointUser.getUsablePoint());
        pointLog.setType(2);  // 2=消耗
        pointLog.setSource(source);
        pointLog.setRemark(remark);
        pointLogMapper.insert(pointLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer sign(Long userId) {
        // 1. Redis签到防重复
        String today = LocalDateTime.now().toLocalDate().toString();
        String signKey = Constants.SIGN_KEY + userId + ":" + today;

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(signKey))) {
            throw BusinessException.of(3002, "今日已完成签到，请勿重复签到");
        }

        // 2. 增加积分
        addPoint(userId, Constants.SIGN_REWARD_POINT, "sign", "每日签到奖励");

        // 3. 设置Redis签到标记（过期至当日23:59:59）
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        Duration duration = Duration.between(LocalDateTime.now(), endOfDay);
        stringRedisTemplate.opsForValue().set(signKey, "1", duration);

        log.info("[签到] userId={}, rewardPoint={}", userId, Constants.SIGN_REWARD_POINT);
        return Constants.SIGN_REWARD_POINT;
    }

    @Override
    public Map<String, Object> signInfo(Long userId) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("signed", false);
        result.put("todayPoint", Constants.SIGN_REWARD_POINT);
        result.put("totalDays", 0);
        return result;
    }

    @Override
    public Map<String, Object> balance(Long userId) {
        LambdaQueryWrapper<PointUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointUser::getUserId, userId);
        PointUser pointUser = pointUserMapper.selectOne(wrapper);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalPoint", pointUser != null ? pointUser.getTotalPoint() : 0);
        result.put("usablePoint", pointUser != null ? pointUser.getUsablePoint() : 0);
        return result;
    }
}
