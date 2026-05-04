package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_info")
public class UserInfo extends BaseEntity {
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer sex;
    private Integer status;
    private String inviteCode;
    private Long parentId;
}
