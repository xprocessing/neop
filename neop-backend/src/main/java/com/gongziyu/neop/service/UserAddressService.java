package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.entity.UserAddress;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {

    List<UserAddress> listByUserId(Long userId);

    void saveAddress(UserAddress address);

    void updateAddress(UserAddress address);

    void deleteAddress(Long id, Long userId);

    void setDefault(Long id, Long userId);
}
