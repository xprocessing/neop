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
@TableName("task_user_receive")
public class TaskUserReceive extends BaseEntity {
    private Long userId;
    private Long taskId;
    private String openid;
    private BigDecimal rewardAmount;
    private Integer auditStatus;
    private Integer grantPay;
    private Integer withdrawStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date expireTime;
}
