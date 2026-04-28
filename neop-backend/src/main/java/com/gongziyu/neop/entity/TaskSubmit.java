package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_submit")
public class TaskSubmit extends BaseEntity {
    private Long receiveId;
    private Long userId;
    private String submitImages;
    private String submitNote;
    private String auditNote;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date auditTime;
}
