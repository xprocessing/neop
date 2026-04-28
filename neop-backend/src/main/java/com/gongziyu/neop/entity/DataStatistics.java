package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_statistics")
public class DataStatistics extends BaseEntity {
    private String statDate;
    private Integer registerNum;
    private Integer activeNum;
    private Integer orderNum;
    private BigDecimal orderAmount;
    private Integer taskNum;
    private BigDecimal taskPayAmount;
}
