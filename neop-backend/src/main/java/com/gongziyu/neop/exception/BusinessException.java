package com.gongziyu.neop.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(500, message);
    }

    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException of(String message) {
        return new BusinessException(500, message);
    }
}
