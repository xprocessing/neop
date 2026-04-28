package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operation_log")
public class SysOperationLog extends BaseEntity {
    private Integer operatorType;
    private Long operatorId;
    private String operatorName;
    private String module;
    private String description;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseResult;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Long costTime;
}
