package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_wechat")
public class UserWechat extends BaseEntity {
    private Long userId;
    private String openid;
    private String unionid;
    private String appType;
}
