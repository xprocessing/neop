package com.gongziyu.neop.controller.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.annotation.OperationLog;
import com.gongziyu.neop.annotation.RateLimit;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.SysAdmin;
import com.gongziyu.neop.service.SysAdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台管理员接口
 */
@RestController
@RequestMapping("/api/admin")
public class SysAdminController {

    @Autowired
    private SysAdminService sysAdminService;

    /**
     * 后台登录
     */
    @PostMapping("/login")
    @RateLimit(key = "admin:login", limit = 5, period = 60)
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        Map<String, Object> result = sysAdminService.login(dto.getUsername(), dto.getPassword());
        return Result.success(result);
    }

    /**
     * 获取管理员信息（含权限）
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        Map<String, Object> result = sysAdminService.getAdminInfo(adminId);
        return Result.success(result);
    }

    /**
     * 管理员分页列表
     */
    @GetMapping("/list")
    @OperationLog(module = "系统管理", description = "查询管理员列表")
    public Result<IPage<SysAdmin>> list(PageDTO pageDTO,
                                         @RequestParam(required = false) String username,
                                         @RequestParam(required = false) Integer status) {
        IPage<SysAdmin> page = sysAdminService.listPage(pageDTO, username, status);
        return Result.success(page);
    }

    /**
     * 新增管理员
     */
    @PostMapping("/save")
    @OperationLog(module = "系统管理", description = "新增管理员")
    public Result<Void> save(@RequestBody SysAdmin admin) {
        sysAdminService.addAdmin(admin);
        return Result.success();
    }

    /**
     * 编辑管理员
     */
    @PostMapping("/update")
    @OperationLog(module = "系统管理", description = "编辑管理员")
    public Result<Void> update(@RequestBody SysAdmin admin) {
        sysAdminService.updateAdmin(admin);
        return Result.success();
    }

    /**
     * 删除管理员
     */
    @PostMapping("/delete")
    @OperationLog(module = "系统管理", description = "删除管理员")
    public Result<Void> delete(@RequestParam Long id) {
        sysAdminService.deleteAdmin(id);
        return Result.success();
    }

    /**
     * 修改状态
     */
    @PostMapping("/status")
    @OperationLog(module = "系统管理", description = "修改管理员状态")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        sysAdminService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    @OperationLog(module = "系统管理", description = "修改密码")
    public Result<Void> updatePassword(@RequestBody PasswordDTO dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        sysAdminService.updatePassword(adminId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @Data
    public static class LoginDTO {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class PasswordDTO {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
