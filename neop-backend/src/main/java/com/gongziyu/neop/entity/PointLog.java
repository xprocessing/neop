package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("point_log")
public class PointLog extends BaseEntity {
    private Long userId;
    private Integer point;
    private Integer balance;
    private Integer type;
    private String source;
    private String remark;
}
