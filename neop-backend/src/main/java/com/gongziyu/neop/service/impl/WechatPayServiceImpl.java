package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gongziyu.neop.entity.TaskPayLog;
import com.gongziyu.neop.entity.TaskUserReceive;
import com.gongziyu.neop.exception.WechatApiException;
import com.gongziyu.neop.mapper.TaskPayLogMapper;
import com.gongziyu.neop.mapper.TaskUserReceiveMapper;
import com.gongziyu.neop.service.WechatPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 微信企业付款服务实现（文档第九章）
 * 使用微信支付V2接口 + HMAC-SHA256签名 + p12证书
 */
@Slf4j
@Service
public class WechatPayServiceImpl implements WechatPayService {

    @Autowired
    private TaskPayLogMapper taskPayLogMapper;

    @Autowired
    private TaskUserReceiveMapper taskUserReceiveMapper;

    @Value("${neop.wechat.mch-id:}")
    private String mchId;

    @Value("${neop.wechat.api-key:}")
    private String apiKey;

    @Value("${neop.wechat.cert-path:}")
    private String certPath;

    @Override
    public String transfer(String tradeNo, String openid, String amount, String desc) {
        try {
            // TODO: 使用weixin-java-pay SDK调用企业付款接口
            // 完整实现需要：
            // 1. 构建请求参数（mch_id, nonce_str, partner_trade_no, openid, check_name, amount, desc）
            // 2. HMAC-SHA256签名
            // 3. 加载p12证书
            // 4. 调用 https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers
            // 5. 解析XML响应

            log.info("[企业付款] tradeNo={}, openid={}, amount={}", tradeNo, openid, amount);

            // 模拟返回微信支付单号
            String wechatPayNo = "wx_pay_" + System.currentTimeMillis();
            return wechatPayNo;

        } catch (Exception e) {
            log.error("[企业付款失败] tradeNo={}, error={}", tradeNo, e.getMessage(), e);
            throw new WechatApiException(5001, "微信打款失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processPay(TaskPayLog payLog) {
        try {
            // 1. 调用微信企业付款
            String wechatPayNo = transfer(
                    payLog.getTradeNo(),
                    getOpenid(payLog.getUserId()),
                    payLog.getPayAmount().toPlainString(),
                    "任务奖励-" + payLog.getTaskTitle()
            );

            // 2. 更新打款日志为成功
            payLog.setPayStatus(2);  // 成功
            payLog.setWechatPayNo(wechatPayNo);
            payLog.setPayTime(new Date());
            taskPayLogMapper.updateById(payLog);

            // 3. 更新任务领取记录提现状态
            TaskUserReceive receive = taskUserReceiveMapper.selectById(payLog.getReceiveId());
            if (receive != null) {
                receive.setWithdrawStatus(2);  // 已成功
                taskUserReceiveMapper.updateById(receive);
            }

            log.info("[打款成功] receiveId={}, tradeNo={}, wechatPayNo={}", payLog.getReceiveId(), payLog.getTradeNo(), wechatPayNo);

        } catch (WechatApiException e) {
            // 打款失败
            payLog.setPayStatus(3);  // 失败
            payLog.setFailReason(e.getMessage());
            payLog.setRetryCount(payLog.getRetryCount() + 1);
            taskPayLogMapper.updateById(payLog);

            log.error("[打款失败] receiveId={}, tradeNo={}, reason={}", payLog.getReceiveId(), payLog.getTradeNo(), e.getMessage());
        }
    }

    private String getOpenid(Long userId) {
        // 从user_wechat表获取openid
        // 简化实现，实际需要注入UserWechatMapper
        return "";
    }
}
