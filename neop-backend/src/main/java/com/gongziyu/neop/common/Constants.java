package com.gongziyu.neop.common;

/**
 * 全局常量定义
 */
public class Constants {

    /** Redis Key前缀 */
    public static final String REDIS_PREFIX = "neop:";

    /** Token黑名单 */
    public static final String TOKEN_BLACKLIST_KEY = REDIS_PREFIX + "token:blacklist:";

    /** 微信回调幂等 */
    public static final String WECHAT_CALLBACK_IDEMPOTENT_KEY = REDIS_PREFIX + "wechat:callback:idempotent:";

    /** 任务领取计数 */
    public static final String TASK_RECEIVE_COUNT_KEY = REDIS_PREFIX + "task:receive:count:userId:";

    /** 任务库存 */
    public static final String TASK_STOCK_KEY = REDIS_PREFIX + "task:stock:";

    /** 签到标记 */
    public static final String SIGN_KEY = REDIS_PREFIX + "sign:userId:";

    /** 限流 */
    public static final String RATE_LIMIT_KEY = REDIS_PREFIX + "rate:limit:";

    /** 字典缓存 */
    public static final String DICT_CACHE_KEY = REDIS_PREFIX + "dict:cache:";

    /** 系统配置缓存 */
    public static final String CONFIG_CACHE_KEY = REDIS_PREFIX + "config:cache:";

    /** 商品库存缓存 */
    public static final String PRODUCT_STOCK_KEY = REDIS_PREFIX + "product:stock:";

    /** 分布式锁前缀 */
    public static final String LOCK_PREFIX = REDIS_PREFIX + "lock:";

    /** 邀请码字符集（排除0/O、1/I/L） */
    public static final String INVITE_CODE_CHARSET = "ABCDEFGHJKMNPQRSTVWXYZ23456789";

    /** 邀请码长度 */
    public static final int INVITE_CODE_LENGTH = 8;

    /** 订单号前缀 */
    public static final String ORDER_NO_PREFIX = "NO";
    public static final String MEMBER_ORDER_NO_PREFIX = "MB";
    public static final String TRADE_NO_PREFIX = "TP";

    /** 签到奖励积分 */
    public static final int SIGN_REWARD_POINT = 10;

    /** 邀请奖励积分 */
    public static final int INVITE_REWARD_POINT = 10;

    /** 订单自动取消时间（分钟） */
    public static final int ORDER_AUTO_CANCEL_MINUTE = 15;

    /** 自动确认收货天数 */
    public static final int AUTO_CONFIRM_DAYS = 7;
}
