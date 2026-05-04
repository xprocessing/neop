package com.gongziyu.neop.controller.user;

import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.UserAddress;
import com.gongziyu.neop.service.UserAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址接口
 */
@RestController
@RequestMapping("/api/user/address")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    /**
     * 收货地址列表
     */
    @GetMapping("/list")
    public Result<List<UserAddress>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<UserAddress> list = userAddressService.listByUserId(userId);
        return Result.success(list);
    }

    /**
     * 新增收货地址
     */
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody AddressDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        userAddressService.saveAddress(address);
        return Result.success();
    }

    /**
     * 编辑收货地址
     */
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody AddressUpdateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserAddress address = new UserAddress();
        address.setId(dto.getId());
        address.setUserId(userId);
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault());
        userAddressService.updateAddress(address);
        return Result.success();
    }

    /**
     * 删除收货地址
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userAddressService.deleteAddress(id, userId);
        return Result.success();
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public Result<UserAddress> getDefault(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserAddress address = userAddressService.getDefault(userId);
        return Result.success(address);
    }

    /**
     * 设为默认地址
     */
    @PostMapping("/setDefault")
    public Result<Void> setDefault(@RequestParam Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userAddressService.setDefault(id, userId);
        return Result.success();
    }

    @Data
    public static class AddressDTO {
        @NotBlank(message = "收货人姓名不能为空")
        private String name;
        @NotBlank(message = "收货人电话不能为空")
        private String phone;
        @NotBlank(message = "省份不能为空")
        private String province;
        @NotBlank(message = "城市不能为空")
        private String city;
        @NotBlank(message = "区县不能为空")
        private String district;
        @NotBlank(message = "详细地址不能为空")
        private String detail;
        private Integer isDefault;
    }

    @Data
    public static class AddressUpdateDTO extends AddressDTO {
        private Long id;
    }
}
