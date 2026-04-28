package com.gongziyu.neop.util;

import com.gongziyu.neop.common.Constants;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 邀请码生成工具类（文档2.5.1节）
 * 格式：8位大写字母+数字，排除易混淆字符（0/O、1/I/L）
 */
public class InviteCodeUtil {

    public static String generate() {
        StringBuilder sb = new StringBuilder(Constants.INVITE_CODE_LENGTH);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < Constants.INVITE_CODE_LENGTH; i++) {
            int index = random.nextInt(Constants.INVITE_CODE_CHARSET.length());
            sb.append(Constants.INVITE_CODE_CHARSET.charAt(index));
        }
        return sb.toString();
    }
}
