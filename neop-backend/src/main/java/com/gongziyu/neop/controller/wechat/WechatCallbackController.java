package com.gongziyu.neop.controller.wechat;

import com.gongziyu.neop.service.MemberPackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 微信支付回调接口
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatCallbackController {

    @Autowired
    private MemberPackageService memberPackageService;

    /**
     * 会员支付回调
     */
    @PostMapping("/member/callback")
    public String memberCallback(@RequestBody String xmlData) {
        try {
            // TODO: 解析XML报文、验签、提取orderNo和wechatPayNo
            // 阶段6实现完整的微信回调解析
            String orderNo = "";
            String wechatPayNo = "";

            memberPackageService.handlePayCallback(orderNo, wechatPayNo);

            return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
        } catch (Exception e) {
            log.error("[会员支付回调] 处理失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";
        }
    }

    /**
     * 电商订单支付回调
     */
    @PostMapping("/shop/callback")
    public String shopCallback(@RequestBody String xmlData) {
        try {
            // TODO: 阶段4实现电商支付回调
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
        } catch (Exception e) {
            log.error("[电商支付回调] 处理失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";
        }
    }
}
