package com.gongziyu.neop.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("invite_reward_log")
public class InviteRewardLog extends BaseEntity {
    private Long userId;
    private Long inviteUserId;
    private Integer rewardType;
    private Integer rewardNum;
    private String remark;
}
