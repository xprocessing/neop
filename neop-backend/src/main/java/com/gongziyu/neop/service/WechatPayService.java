package com.gongziyu.neop.service;

import com.gongziyu.neop.entity.TaskPayLog;

import java.util.Map;

public interface WechatPayService {

    /**
     * 微信企业付款到零钱（文档9.1-9.2节）
     * @param tradeNo 本地交易单号
     * @param openid 用户openid
     * @param amount 金额（元）
     * @param desc 描述
     * @return 微信支付单号
     */
    String transfer(String tradeNo, String openid, String amount, String desc);

    /**
     * 处理打款（完整流程：校验→调用微信→更新状态）
     */
    void processPay(TaskPayLog payLog);
}
