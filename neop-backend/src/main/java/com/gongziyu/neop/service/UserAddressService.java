package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.entity.UserAddress;

import java.util.List;
import com.gongziyu.neop.entity.UserAddress;

public interface UserAddressService extends IService<UserAddress> {

    List<UserAddress> listByUserId(Long userId);

    UserAddress getDefault(Long userId);

    void saveAddress(UserAddress address);

    void updateAddress(UserAddress address);

    void deleteAddress(Long id, Long userId);

    void setDefault(Long id, Long userId);
}
