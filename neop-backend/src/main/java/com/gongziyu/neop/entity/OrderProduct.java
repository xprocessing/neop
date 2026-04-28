package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_product")
public class OrderProduct extends BaseEntity {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImg;
    private BigDecimal price;
    private Integer num;
    private BigDecimal subtotalAmount;
}
