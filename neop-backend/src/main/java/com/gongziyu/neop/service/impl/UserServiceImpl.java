package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongziyu.neop.common.Constants;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.*;
import com.gongziyu.neop.exception.BusinessException;
import com.gongziyu.neop.mapper.*;
import com.gongziyu.neop.service.PointService;
import com.gongziyu.neop.service.UserService;
import com.gongziyu.neop.util.InviteCodeUtil;
import com.gongziyu.neop.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserWechatMapper userWechatMapper;

    @Autowired
    private InviteUserMapper inviteUserMapper;

    @Autowired
    private InviteRewardLogMapper inviteRewardLogMapper;

    @Autowired
    private PointUserMapper pointUserMapper;

    @Autowired
    private MemberUserMapper memberUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PointService pointService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> wechatLogin(String code, String inviteCode) {
        // 1. 调用微信接口通过code换取openid（此处需要微信SDK，阶段3实现完整版本）
        // TODO: 调用微信API获取openid和session_key
        String openid = "temp_openid_" + code;  // 临时占位，阶段6实现完整微信接口

        // 2. 根据openid查询用户
        LambdaQueryWrapper<UserWechat> wechatWrapper = new LambdaQueryWrapper<>();
        wechatWrapper.eq(UserWechat::getOpenid, openid);
        UserWechat userWechat = userWechatMapper.selectOne(wechatWrapper);

        UserInfo user;
        boolean isNewUser = false;

        if (userWechat != null) {
            // 已有用户，直接登录
            user = userInfoMapper.selectById(userWechat.getUserId());
        } else {
            // 新用户，自动注册
            isNewUser = true;
            user = new UserInfo();
            user.setNickname("用户" + System.currentTimeMillis() % 100000);
            user.setInviteCode(InviteCodeUtil.generate());
            user.setStatus(1);
            user.setSex(0);
            userInfoMapper.insert(user);

            // 绑定微信openid
            userWechat = new UserWechat();
            userWechat.setUserId(user.getId());
            userWechat.setOpenid(openid);
            userWechat.setAppType("mp");
            userWechatMapper.insert(userWechat);

            // 初始化积分账户
            PointUser pointUser = new PointUser();
            pointUser.setUserId(user.getId());
            pointUser.setTotalPoint(0);
            pointUser.setUsablePoint(0);
            pointUserMapper.insert(pointUser);

            // 处理邀请关系
            if (StringUtils.isNotBlank(inviteCode)) {
                handleInvite(user.getId(), inviteCode);
            }

            log.info("[微信登录] 新用户注册 userId={}, inviteCode={}", user.getId(), user.getInviteCode());
        }

        // 3. 签发移动端JWT Token
        String token = jwtUtil.generateMobileToken(user.getId(), user.getNickname(), user.getInviteCode());

        // 4. 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", buildUserInfo(user));

        return result;
    }

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.of(401, "用户不存在");
        }
        return buildUserInfo(user);
    }

    @Override
    public IPage<UserInfo> listPage(PageDTO pageDTO, String nickname, String phone, Integer memberStatus) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(nickname)) {
            wrapper.like(UserInfo::getNickname, nickname);
        }
        if (StringUtils.isNotBlank(phone)) {
            wrapper.eq(UserInfo::getPhone, phone);
        }
        wrapper.orderByDesc(UserInfo::getCreateTime);
        return userInfoMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    /**
     * 处理邀请关系
     */
    private void handleInvite(Long userId, String inviteCode) {
        // 查找邀请人
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getInviteCode, inviteCode);
        UserInfo inviter = userInfoMapper.selectOne(wrapper);

        if (inviter == null || inviter.getId().equals(userId)) {
            log.warn("[邀请] 邀请码无效 inviteCode={}", inviteCode);
            return;
        }

        // 记录邀请关系
        InviteUser inviteUser = new InviteUser();
        inviteUser.setUserId(userId);
        inviteUser.setParentId(inviter.getId());
        inviteUser.setInviteNum(0);
        inviteUserMapper.insert(inviteUser);

        // 更新用户上级
        UserInfo updateUser = new UserInfo();
        updateUser.setId(userId);
        updateUser.setParentId(inviter.getId());
        userInfoMapper.updateById(updateUser);

        // 更新邀请人邀请数
        LambdaQueryWrapper<InviteUser> inviterWrapper = new LambdaQueryWrapper<>();
        inviterWrapper.eq(InviteUser::getUserId, inviter.getId());
        InviteUser inviterInvite = inviteUserMapper.selectOne(inviterWrapper);
        if (inviterInvite != null) {
            inviterInvite.setInviteNum(inviterInvite.getInviteNum() + 1);
            inviteUserMapper.updateById(inviterInvite);
        }

        // 发放邀请奖励积分
        pointService.addPoint(inviter.getId(), Constants.INVITE_REWARD_POINT, "invite", "邀请用户注册奖励");

        // 记录邀请奖励
        InviteRewardLog rewardLog = new InviteRewardLog();
        rewardLog.setUserId(inviter.getId());
        rewardLog.setInviteUserId(userId);
        rewardLog.setRewardType(1);  // 1=积分
        rewardLog.setRewardNum(Constants.INVITE_REWARD_POINT);
        rewardLog.setRemark("邀请用户注册奖励");
        inviteRewardLogMapper.insert(rewardLog);

        log.info("[邀请] 邀请成功 inviterId={}, newUserId={}, rewardPoint={}", inviter.getId(), userId, Constants.INVITE_REWARD_POINT);
    }

    /**
     * 构建用户信息返回
     */
    private Map<String, Object> buildUserInfo(UserInfo user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("phone", user.getPhone());
        info.put("inviteCode", user.getInviteCode());
        info.put("sex", user.getSex());

        // 积分信息
        LambdaQueryWrapper<PointUser> pointWrapper = new LambdaQueryWrapper<>();
        pointWrapper.eq(PointUser::getUserId, user.getId());
        PointUser pointUser = pointUserMapper.selectOne(pointWrapper);
        info.put("usablePoint", pointUser != null ? pointUser.getUsablePoint() : 0);

        // 会员信息
        LambdaQueryWrapper<MemberUser> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(MemberUser::getUserId, user.getId());
        MemberUser memberUser = memberUserMapper.selectOne(memberWrapper);
        info.put("memberStatus", memberUser != null ? memberUser.getIsMember() : 0);
        info.put("memberExpireTime", memberUser != null ? memberUser.getExpireTime() : null);

        return info;
    }
}
