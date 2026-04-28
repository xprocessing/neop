package com.gongziyu.neop.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.annotation.OperationLog;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.SysDict;
import com.gongziyu.neop.entity.SysDictData;
import com.gongziyu.neop.mapper.SysDictDataMapper;
import com.gongziyu.neop.mapper.SysDictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理接口
 */
@RestController
@RequestMapping("/api/admin/dict")
public class SysDictController {

    @Autowired
    private SysDictMapper sysDictMapper;

    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    @GetMapping("/list")
    public Result<IPage<SysDict>> list(PageDTO pageDTO) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysDict::getCreateTime);
        IPage<SysDict> page = sysDictMapper.selectPage(pageDTO.getPage(), wrapper);
        return Result.success(page);
    }

    @PostMapping("/save")
    @OperationLog(module = "系统管理", description = "新增字典")
    public Result<Void> save(@RequestBody SysDict dict) {
        sysDictMapper.insert(dict);
        return Result.success();
    }

    @PostMapping("/update")
    @OperationLog(module = "系统管理", description = "编辑字典")
    public Result<Void> update(@RequestBody SysDict dict) {
        sysDictMapper.updateById(dict);
        return Result.success();
    }

    @PostMapping("/delete")
    @OperationLog(module = "系统管理", description = "删除字典")
    public Result<Void> delete(@RequestParam Long id) {
        sysDictMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 根据字典编码查询字典数据
     */
    @GetMapping("/data/{dictCode}")
    public Result<List<SysDictData>> dictData(@PathVariable String dictCode) {
        LambdaQueryWrapper<SysDict> dictWrapper = new LambdaQueryWrapper<>();
        dictWrapper.eq(SysDict::getDictCode, dictCode);
        SysDict dict = sysDictMapper.selectOne(dictWrapper);

        if (dict == null) {
            return Result.success(List.of());
        }

        LambdaQueryWrapper<SysDictData> dataWrapper = new LambdaQueryWrapper<>();
        dataWrapper.eq(SysDictData::getDictId, dict.getId())
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSort);
        List<SysDictData> list = sysDictDataMapper.selectList(dataWrapper);
        return Result.success(list);
    }
}
