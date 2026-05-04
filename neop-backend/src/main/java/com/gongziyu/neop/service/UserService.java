package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.UserInfo;

import java.util.Map;

public interface UserService extends IService<UserInfo> {

    /**
     * 微信登录（自动注册）
     */
    Map<String, Object> wechatLogin(String code, String inviteCode);

    /**
     * 获取用户个人信息
     */
    Map<String, Object> getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateUser(Long userId, Map<String, Object> body);

    /**
     * 绑定手机号
     */
    void bindPhone(Long userId, String phone);

    /**
     * 刷新Token
     */
    Map<String, Object> refreshToken(String refreshToken);

    /**
     * 邀请信息
     */
    Map<String, Object> inviteInfo(Long userId);

    /**
     * 邀请列表
     */
    IPage<Map<String, Object>> inviteList(Long userId, PageDTO pageDTO);

    /**
     * 邀请二维码
     */
    Map<String, Object> inviteQrcode(Long userId);

    /**
     * 分页查询用户列表（后台）
     */
    IPage<UserInfo> listPage(PageDTO pageDTO, String nickname, String phone, Integer memberStatus);
}
