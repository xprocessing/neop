package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_package")
public class MemberPackage extends BaseEntity {
    private String packageName;
    private BigDecimal price;
    private Integer dayNum;
    private Integer givePoint;
    private Integer sort;
    private Integer status;
}
