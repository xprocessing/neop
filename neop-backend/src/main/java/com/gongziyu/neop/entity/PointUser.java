package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("point_user")
public class PointUser extends BaseEntity {
    private Long userId;
    private Integer totalPoint;
    private Integer usablePoint;
}
