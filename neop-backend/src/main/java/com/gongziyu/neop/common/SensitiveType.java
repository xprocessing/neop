package com.gongziyu.neop.common;

/**
 * 敏感数据脱敏类型枚举（文档8.1.4节）
 */
public enum SensitiveType {
    /** 手机号：138****1234 */
    PHONE,
    /** 身份证：310***********1234 */
    ID_CARD,
    /** 地址：浦东***路100号 */
    ADDRESS,
    /** 微信openid：o6kMp1****abcd */
    OPENID
}
