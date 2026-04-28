package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.MemberPackage;

import java.util.Map;

public interface MemberPackageService extends IService<MemberPackage> {

    /**
     * 会员套餐列表（前台，只查启用的）
     */
    IPage<MemberPackage> frontList(PageDTO pageDTO);

    /**
     * 会员套餐列表（后台）
     */
    IPage<MemberPackage> adminList(PageDTO pageDTO, String packageName, Integer status);

    /**
     * 新增套餐
     */
    void addPackage(MemberPackage memberPackage);

    /**
     * 编辑套餐
     */
    void updatePackage(MemberPackage memberPackage);

    /**
     * 删除套餐
     */
    void deletePackage(Long id);

    /**
     * 修改状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 会员充值下单
     */
    Map<String, Object> createOrder(Long userId, Long packageId);

    /**
     * 会员订单支付（调用微信统一下单）
     */
    Map<String, Object> payOrder(Long userId, Long orderId);

    /**
     * 会员支付回调处理
     */
    void handlePayCallback(String orderNo, String wechatPayNo);

    /**
     * 延长会员到期时间（按2.5.3叠加规则）
     */
    void extendMember(Long userId, Long packageId);

    /**
     * 发放套餐赠送积分
     */
    void grantPackagePoint(Long userId, Long packageId);
}
