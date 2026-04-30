package com.gongziyu.neop.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.SysRole;
import com.gongziyu.neop.entity.SysRoleMenu;
import com.gongziyu.neop.mapper.SysRoleMapper;
import com.gongziyu.neop.mapper.SysRoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        List<SysRole> list = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSort));
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        SysRole role = new SysRole();
        role.setRoleName((String) body.get("roleName"));
        role.setRoleKey((String) body.getOrDefault("roleCode", ""));
        role.setSort(body.get("sort") != null ? ((Number) body.get("sort")).intValue() : 0);
        role.setStatus(body.get("status") != null ? ((Number) body.get("status")).intValue() : 1);
        sysRoleMapper.insert(role);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Map<String, Object> body) {
        SysRole role = new SysRole();
        role.setId(((Number) body.get("id")).longValue());
        role.setRoleName((String) body.get("roleName"));
        role.setRoleKey((String) body.getOrDefault("roleCode", ""));
        role.setSort(body.get("sort") != null ? ((Number) body.get("sort")).intValue() : 0);
        role.setStatus(body.get("status") != null ? ((Number) body.get("status")).intValue() : 1);
        sysRoleMapper.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/permissions/{id}")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        List<SysRoleMenu> list = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return Result.success(list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()));
    }

    @PostMapping("/assign-permissions")
    public Result<Void> assignPermissions(@RequestBody Map<String, Object> body) {
        Long roleId = ((Number) body.get("roleId")).longValue();
        @SuppressWarnings("unchecked")
        List<Integer> menuIds = (List<Integer>) body.get("menuIds");

        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));

        for (Integer menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId.longValue());
            sysRoleMenuMapper.insert(rm);
        }
        return Result.success();
    }
}
