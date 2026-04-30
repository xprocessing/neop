package com.gongziyu.neop.exception;

import com.gongziyu.neop.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器（文档20.2节）
 * 异常按优先级顺序捕获处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常（最高优先级）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String firstError = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        log.warn("[参数校验异常] {}", firstError);
        return Result.error(400, firstError);
    }

    /**
     * 数据绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String firstError = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数绑定失败";
        log.warn("[绑定异常] {}", firstError);
        return Result.error(400, firstError);
    }

    /**
     * 自定义参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[参数异常] {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 微信API调用异常
     */
    @ExceptionHandler(WechatApiException.class)
    public Result<Void> handleWechatException(WechatApiException e) {
        log.error("[微信API异常] code={}, msg={}", e.getCode(), e.getMessage(), e);
        return Result.error(500, "微信服务异常，请稍后重试");
    }

    /**
     * 分布式锁获取失败异常
     */
    @ExceptionHandler(LockAcquisitionException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleLockException(LockAcquisitionException e) {
        log.warn("[锁获取失败] {}", e.getMessage());
        return Result.error(429, "操作过于频繁，请稍后再试");
    }

    /**
     * 数据库操作异常
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccessException(DataAccessException e) {
        log.error("[数据库异常]", e);
        return Result.error(500, "数据库操作异常，请联系管理员");
    }

    /**
     * 404资源未找到
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("[404] {}", e.getResourcePath());
        return Result.error(404, "接口不存在");
    }

    /**
     * 未知异常（最低优先级，必须放在最后）
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknownException(Exception e) {
        log.error("[未知异常]", e);
        return Result.error(500, "服务器内部异常");
    }
}
