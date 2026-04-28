package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.*;
import com.gongziyu.neop.exception.BusinessException;
import com.gongziyu.neop.mapper.*;
import com.gongziyu.neop.service.SysAdminService;
import com.gongziyu.neop.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysAdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements SysAdminService {

    @Autowired
    private SysAdminMapper sysAdminMapper;

    @Autowired
    private SysAdminRoleMapper sysAdminRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> login(String username, String password) {
        // 1. 查询管理员
        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAdmin::getUsername, username);
        SysAdmin admin = sysAdminMapper.selectOne(wrapper);

        if (admin == null) {
            throw BusinessException.of(401, "账号不存在");
        }
        if (admin.getStatus() == 0) {
            throw BusinessException.of(401, "账号已禁用");
        }

        // 2. 校验密码（BCrypt）
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw BusinessException.of(401, "密码错误");
        }

        // 3. 签发Token
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername(), admin.getNickname());

        // 4. 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", admin.getId());
        adminInfo.put("username", admin.getUsername());
        adminInfo.put("nickname", admin.getNickname());
        adminInfo.put("avatar", admin.getAvatar());
        result.put("adminInfo", adminInfo);

        return result;
    }

    @Override
    public Map<String, Object> getAdminInfo(Long adminId) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw BusinessException.of(401, "管理员不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("nickname", admin.getNickname());
        result.put("avatar", admin.getAvatar());

        // 获取权限列表
        List<String> permissions = getPermissions(adminId);
        result.put("permissions", permissions);

        return result;
    }

    @Override
    public IPage<SysAdmin> listPage(PageDTO pageDTO, String username, Integer status) {
        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            wrapper.like(SysAdmin::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysAdmin::getStatus, status);
        }
        wrapper.orderByDesc(SysAdmin::getCreateTime);
        return sysAdminMapper.selectPage(pageDTO.getPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdmin(SysAdmin admin) {
        // 检查用户名唯一性
        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAdmin::getUsername, admin.getUsername());
        if (sysAdminMapper.selectCount(wrapper) > 0) {
            throw BusinessException.of(400, "用户名已存在");
        }
        // BCrypt加密密码
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        sysAdminMapper.insert(admin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdmin(SysAdmin admin) {
        SysAdmin existing = sysAdminMapper.selectById(admin.getId());
        if (existing == null) {
            throw BusinessException.of(400, "管理员不存在");
        }
        // 如果修改了密码，需要加密
        if (StringUtils.isNotBlank(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        } else {
            admin.setPassword(null);  // 不修改密码
        }
        sysAdminMapper.updateById(admin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Long id) {
        sysAdminMapper.deleteById(id);
        // 删除关联角色
        LambdaQueryWrapper<SysAdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAdminRole::getAdminId, id);
        sysAdminRoleMapper.delete(wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysAdmin admin = new SysAdmin();
        admin.setId(id);
        admin.setStatus(status);
        sysAdminMapper.updateById(admin);
    }

    @Override
    public void updatePassword(Long adminId, String oldPassword, String newPassword) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw BusinessException.of(400, "管理员不存在");
        }
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw BusinessException.of(400, "原密码错误");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        sysAdminMapper.updateById(admin);
    }

    /**
     * 获取管理员权限列表
     */
    private List<String> getPermissions(Long adminId) {
        // 1. 查询管理员关联的角色ID
        LambdaQueryWrapper<SysAdminRole> adminRoleWrapper = new LambdaQueryWrapper<>();
        adminRoleWrapper.eq(SysAdminRole::getAdminId, adminId);
        List<SysAdminRole> adminRoles = sysAdminRoleMapper.selectList(adminRoleWrapper);
        List<Long> roleIds = adminRoles.stream().map(SysAdminRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 查询角色关联的菜单ID
        LambdaQueryWrapper<SysRoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.in(SysRoleMenu::getRoleId, roleIds);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(roleMenuWrapper);
        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 查询菜单权限标识
        LambdaQueryWrapper<SysMenu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.in(SysMenu::getId, menuIds)
                .isNotNull(SysMenu::getPermission)
                .ne(SysMenu::getPermission, "");
        List<SysMenu> menus = sysMenuMapper.selectList(menuWrapper);

        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }
}
