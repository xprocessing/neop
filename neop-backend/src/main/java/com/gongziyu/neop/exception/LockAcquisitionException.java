package com.gongziyu.neop.exception;

/**
 * 分布式锁获取失败异常
 */
public class LockAcquisitionException extends RuntimeException {

    public LockAcquisitionException(String message) {
        super(message);
    }
}
