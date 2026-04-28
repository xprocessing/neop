package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.MemberPackage;
import com.gongziyu.neop.entity.MemberUser;
import com.gongziyu.neop.entity.PayOrder;
import com.gongziyu.neop.exception.BusinessException;
import com.gongziyu.neop.mapper.MemberPackageMapper;
import com.gongziyu.neop.mapper.MemberUserMapper;
import com.gongziyu.neop.mapper.PayOrderMapper;
import com.gongziyu.neop.service.MemberPackageService;
import com.gongziyu.neop.service.PointService;
import com.gongziyu.neop.util.OrderNoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MemberPackageServiceImpl extends ServiceImpl<MemberPackageMapper, MemberPackage> implements MemberPackageService {

    @Autowired
    private MemberPackageMapper memberPackageMapper;

    @Autowired
    private MemberUserMapper memberUserMapper;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private PointService pointService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public IPage<MemberPackage> frontList(PageDTO pageDTO) {
        LambdaQueryWrapper<MemberPackage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPackage::getStatus, 1)
                .orderByAsc(MemberPackage::getSort);
        return memberPackageMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    @Override
    public IPage<MemberPackage> adminList(PageDTO pageDTO, String packageName, Integer status) {
        LambdaQueryWrapper<MemberPackage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(packageName)) {
            wrapper.like(MemberPackage::getPackageName, packageName);
        }
        if (status != null) {
            wrapper.eq(MemberPackage::getStatus, status);
        }
        wrapper.orderByAsc(MemberPackage::getSort);
        return memberPackageMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPackage(MemberPackage memberPackage) {
        memberPackageMapper.insert(memberPackage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePackage(MemberPackage memberPackage) {
        memberPackageMapper.updateById(memberPackage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePackage(Long id) {
        memberPackageMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        MemberPackage pkg = new MemberPackage();
        pkg.setId(id);
        pkg.setStatus(status);
        memberPackageMapper.updateById(pkg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Long userId, Long packageId) {
        // 1. 校验套餐
        MemberPackage pkg = memberPackageMapper.selectById(packageId);
        if (pkg == null || pkg.getStatus() == 0) {
            throw BusinessException.of(3001, "会员套餐不存在或已禁用");
        }

        // 2. 生成订单
        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setOrderNo(OrderNoUtil.generateMemberOrderNo());
        order.setPayType(1);  // 微信支付
        order.setOrderType(1);  // 会员充值
        order.setAmount(pkg.getPrice());
        order.setPackageId(packageId);
        order.setStatus(0);  // 待支付
        payOrderMapper.insert(order);

        // 3. 返回订单信息
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("amount", pkg.getPrice());
        return result;
    }

    @Override
    public Map<String, Object> payOrder(Long userId, Long orderId) {
        // 1. 校验订单
        PayOrder order = payOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw BusinessException.of(5001, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw BusinessException.of(5002, "订单已支付，无需重复支付");
        }

        // 2. 调用微信统一下单（阶段6实现完整版本，此处返回模拟数据）
        // TODO: 调用微信统一下单API，获取prepay_id
        Map<String, Object> payParams = new HashMap<>();
        payParams.put("orderNo", order.getOrderNo());
        payParams.put("amount", order.getAmount());

        log.info("[会员支付] 创建支付订单 orderNo={}, amount={}", order.getOrderNo(), order.getAmount());
        return payParams;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void handlePayCallback(String orderNo, String wechatPayNo) {
        // 1. Redis幂等校验
        String idempotentKey = "neop:wechat:callback:idempotent:" + orderNo;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(idempotentKey))) {
            log.info("[会员支付回调] 重复回调，直接返回成功 orderNo={}", orderNo);
            return;
        }

        // 2. 查询订单
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getOrderNo, orderNo);
        PayOrder order = payOrderMapper.selectOne(wrapper);

        if (order == null || order.getStatus() == 1) {
            // 订单不存在或已支付，设置幂等标记后返回
            stringRedisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);
            return;
        }

        // 3. 更新订单状态
        order.setStatus(1);  // 已支付
        order.setPayTime(new Date());
        payOrderMapper.updateById(order);

        // 4. 延长会员到期时间
        extendMember(order.getUserId(), order.getPackageId());

        // 5. 发放套餐赠送积分
        grantPackagePoint(order.getUserId(), order.getPackageId());

        // 6. 设置幂等标记（事务提交后执行）
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                stringRedisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);
                log.info("[会员支付回调] 幂等标记已设置 orderNo={}", orderNo);
            }
        });

        log.info("[会员支付回调] 处理成功 orderNo={}, wechatPayNo={}", orderNo, wechatPayNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extendMember(Long userId, Long packageId) {
        MemberPackage pkg = memberPackageMapper.selectById(packageId);
        if (pkg == null) {
            return;
        }

        // 查询用户会员信息
        LambdaQueryWrapper<MemberUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberUser::getUserId, userId);
        MemberUser memberUser = memberUserMapper.selectOne(wrapper);

        LocalDateTime now = LocalDateTime.now();

        if (memberUser == null) {
            // 新建会员记录
            memberUser = new MemberUser();
            memberUser.setUserId(userId);
            memberUser.setIsMember(1);
            memberUser.setExpireTime(java.sql.Timestamp.valueOf(now.plusDays(pkg.getDayNum())));
            memberUserMapper.insert(memberUser);
        } else {
            // 按2.5.3叠加规则计算到期时间
            LocalDateTime currentExpire = memberUser.getExpireTime() != null
                    ? memberUser.getExpireTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                    : now;

            LocalDateTime newExpireTime;
            if (memberUser.getIsMember() == 1 && currentExpire.isAfter(now)) {
                // 会员未过期：新到期时间 = 原到期时间 + 套餐天数
                newExpireTime = currentExpire.plusDays(pkg.getDayNum());
            } else {
                // 非会员或已过期：新到期时间 = 当前时间 + 套餐天数
                newExpireTime = now.plusDays(pkg.getDayNum());
            }

            memberUser.setIsMember(1);
            memberUser.setExpireTime(java.sql.Timestamp.valueOf(newExpireTime));
            memberUserMapper.updateById(memberUser);
        }

        log.info("[会员延长] userId={}, packageId={}, dayNum={}", userId, packageId, pkg.getDayNum());
    }

    @Override
    public void grantPackagePoint(Long userId, Long packageId) {
        MemberPackage pkg = memberPackageMapper.selectById(packageId);
        if (pkg != null && pkg.getGivePoint() > 0) {
            pointService.addPoint(userId, pkg.getGivePoint(), "member", "购买会员套餐赠送积分");
        }
    }
}
