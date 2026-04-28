package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.SysAdmin;

import java.util.Map;

public interface SysAdminService extends IService<SysAdmin> {

    /**
     * 管理员登录
     */
    Map<String, Object> login(String username, String password);

    /**
     * 获取管理员信息（含权限列表）
     */
    Map<String, Object> getAdminInfo(Long adminId);

    /**
     * 分页查询管理员列表
     */
    IPage<SysAdmin> listPage(PageDTO pageDTO, String username, Integer status);

    /**
     * 新增管理员
     */
    void addAdmin(SysAdmin admin);

    /**
     * 修改管理员
     */
    void updateAdmin(SysAdmin admin);

    /**
     * 删除管理员
     */
    void deleteAdmin(Long id);

    /**
     * 修改管理员状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 修改密码
     */
    void updatePassword(Long adminId, String oldPassword, String newPassword);
}
