package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongziyu.neop.entity.UserAddress;
import com.gongziyu.neop.exception.BusinessException;
import com.gongziyu.neop.mapper.UserAddressMapper;
import com.gongziyu.neop.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public List<UserAddress> listByUserId(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAddress(UserAddress address) {
        // 如果设为默认地址，先取消原默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelDefault(address.getUserId());
        }
        save(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(UserAddress address) {
        // 校验地址属于当前用户
        UserAddress existing = getById(address.getId());
        if (existing == null || !existing.getUserId().equals(address.getUserId())) {
            throw BusinessException.of(6001, "收货地址不存在");
        }

        // 如果设为默认地址，先取消原默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelDefault(address.getUserId());
        }
        updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long id, Long userId) {
        UserAddress existing = getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw BusinessException.of(6001, "收货地址不存在");
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id, Long userId) {
        UserAddress existing = getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw BusinessException.of(6001, "收货地址不存在");
        }

        // 先取消原默认
        cancelDefault(userId);

        // 设置新默认
        UserAddress update = new UserAddress();
        update.setId(id);
        update.setIsDefault(1);
        updateById(update);
    }

    private void cancelDefault(Long userId) {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0);
        update(wrapper);
    }
}
