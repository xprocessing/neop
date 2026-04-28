package com.gongziyu.neop.config;

import com.alibaba.fastjson2.JSON;
import com.gongziyu.neop.annotation.OperationLog;
import com.gongziyu.neop.entity.SysOperationLog;
import com.gongziyu.neop.mapper.SysOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志AOP切面（文档14.2节）
 * 自动记录后台管理接口的操作日志
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private SysOperationLogMapper sysOperationLogMapper;

    @Around("@annotation(com.gongziyu.neop.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        SysOperationLog operationLog = new SysOperationLog();
        operationLog.setStatus(1);  // 默认成功

        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog annotation = method.getAnnotation(OperationLog.class);
            operationLog.setModule(annotation.module());
            operationLog.setDescription(annotation.description());

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setIp(getIp(request));

                // 获取操作者信息
                Object adminId = request.getAttribute("adminId");
                Object userId = request.getAttribute("userId");
                if (adminId != null) {
                    operationLog.setOperatorType(1);
                    operationLog.setOperatorId(Long.parseLong(adminId.toString()));
                    operationLog.setOperatorName(request.getAttribute("username") != null ? request.getAttribute("username").toString() : "");
                } else if (userId != null) {
                    operationLog.setOperatorType(2);
                    operationLog.setOperatorId(Long.parseLong(userId.toString()));
                }
            }

            // 获取请求参数
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                try {
                    String params = JSON.toJSONString(args[0]);
                    // 截取过长参数
                    if (params.length() > 2000) {
                        params = params.substring(0, 2000) + "...";
                    }
                    operationLog.setRequestParams(params);
                } catch (Exception e) {
                    operationLog.setRequestParams("参数序列化失败");
                }
            }

            // 执行方法
            Object result = joinPoint.proceed();

            // 记录响应结果
            if (result != null) {
                try {
                    String resultStr = JSON.toJSONString(result);
                    if (resultStr.length() > 2000) {
                        resultStr = resultStr.substring(0, 2000) + "...";
                    }
                    operationLog.setResponseResult(resultStr);
                } catch (Exception e) {
                    // 忽略序列化异常
                }
            }

            return result;
        } catch (Exception e) {
            operationLog.setStatus(0);
            operationLog.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 500
                    ? e.getMessage().substring(0, 500) : e.getMessage());
            throw e;
        } finally {
            operationLog.setCostTime(System.currentTimeMillis() - startTime);
            try {
                sysOperationLogMapper.insert(operationLog);
            } catch (Exception e) {
                log.error("[操作日志] 保存失败", e);
            }
        }
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
