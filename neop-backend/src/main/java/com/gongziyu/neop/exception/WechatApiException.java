package com.gongziyu.neop.exception;

import lombok.Getter;

/**
 * 微信API调用异常
 */
@Getter
public class WechatApiException extends RuntimeException {

    private final Integer code;

    public WechatApiException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public WechatApiException(String message) {
        this(5001, message);
    }
}
