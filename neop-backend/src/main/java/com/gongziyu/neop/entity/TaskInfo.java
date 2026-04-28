package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_info")
public class TaskInfo extends BaseEntity {
    private String taskTitle;
    private String taskCover;
    private String taskIntro;
    private String taskContent;
    private BigDecimal rewardAmount;
    private Integer totalNum;
    private Integer dayNum;
    private Integer expireMinute;
    private Integer sort;
    private Integer status;
}
