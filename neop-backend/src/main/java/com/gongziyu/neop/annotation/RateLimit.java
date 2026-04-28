package com.gongziyu.neop.annotation;

import java.lang.annotation.*;

/**
 * 限流注解（文档8.1.3节）
 * 使用Redis滑动窗口实现接口限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流Key（如：task:receive）
     */
    String key() default "";

    /**
     * 限流次数
     */
    int limit() default 10;

    /**
     * 限流时间窗口（秒）
     */
    int period() default 1;
}
