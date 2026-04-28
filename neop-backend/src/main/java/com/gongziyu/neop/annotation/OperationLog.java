package com.gongziyu.neop.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解（文档14.2节）
 * 标注在后台管理接口上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作描述
     */
    String description() default "";
}
