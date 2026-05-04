package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.entity.PointLog;
import com.gongziyu.neop.entity.PointUser;
import java.util.Map;

public interface PointService extends IService<PointUser> {

    /**
     * 增加积分
     * @param userId 用户ID
     * @param point 积分数量
     * @param source 来源（sign/invite/member/task）
     * @param remark 备注
     */
    void addPoint(Long userId, Integer point, String source, String remark);

    /**
     * 消耗积分
     */
    void deductPoint(Long userId, Integer point, String source, String remark);

    /**
     * 签到
     */
    Integer sign(Long userId);

    /**
     * 签到信息
     */
    Map<String, Object> signInfo(Long userId);

    /**
     * 积分余额
     */
    Map<String, Object> balance(Long userId);
}
