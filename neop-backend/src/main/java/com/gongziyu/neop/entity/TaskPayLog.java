package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_pay_log")
public class TaskPayLog extends BaseEntity {
    private Long receiveId;
    private Long userId;
    private String taskTitle;
    private String tradeNo;
    private String wechatPayNo;
    private BigDecimal payAmount;
    private Integer payStatus;
    private String failReason;
    private Integer retryCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date applyTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date payTime;
}
