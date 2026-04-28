package com.gongziyu.neop.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.annotation.OperationLog;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.SysConfig;
import com.gongziyu.neop.mapper.SysConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置管理接口
 */
@RestController
@RequestMapping("/api/admin/config")
public class SysConfigController {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @GetMapping("/list")
    public Result<IPage<SysConfig>> list(PageDTO pageDTO) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysConfig::getCreateTime);
        IPage<SysConfig> page = sysConfigMapper.selectPage(pageDTO.getPage(), wrapper);
        return Result.success(page);
    }

    @PostMapping("/save")
    @OperationLog(module = "系统管理", description = "新增配置")
    public Result<Void> save(@RequestBody SysConfig config) {
        sysConfigMapper.insert(config);
        return Result.success();
    }

    @PostMapping("/update")
    @OperationLog(module = "系统管理", description = "编辑配置")
    public Result<Void> update(@RequestBody SysConfig config) {
        sysConfigMapper.updateById(config);
        return Result.success();
    }

    @PostMapping("/delete")
    @OperationLog(module = "系统管理", description = "删除配置")
    public Result<Void> delete(@RequestParam Long id) {
        sysConfigMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 根据key获取配置值
     */
    @GetMapping("/value/{key}")
    public Result<String> getValue(@PathVariable String key) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, key);
        SysConfig config = sysConfigMapper.selectOne(wrapper);
        return Result.success(config != null ? config.getConfigValue() : null);
    }
}
