package com.gongziyu.neop.util;

import com.gongziyu.neop.common.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单号/交易号生成工具类（文档2.5.2节）
 */
public class OrderNoUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 生成电商订单号（前缀NO + 时间 + 6位随机数）
     */
    public static String generateOrderNo() {
        return generate(Constants.ORDER_NO_PREFIX);
    }

    /**
     * 生成会员充值订单号（前缀MB + 时间 + 6位随机数）
     */
    public static String generateMemberOrderNo() {
        return generate(Constants.MEMBER_ORDER_NO_PREFIX);
    }

    /**
     * 生成任务交易单号（前缀TP + 时间 + 6位随机数）
     */
    public static String generateTradeNo() {
        return generate(Constants.TRADE_NO_PREFIX);
    }

    private static String generate(String prefix) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return prefix + timestamp + random;
    }
}
