package com.gongziyu.neop.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.SysOperationLog;
import com.gongziyu.neop.mapper.SysOperationLogMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志接口
 */
@RestController
@RequestMapping("/api/admin/log")
public class SysOperationLogController {

    @Autowired
    private SysOperationLogMapper sysOperationLogMapper;

    @GetMapping("/list")
    public Result<IPage<SysOperationLog>> list(PageDTO pageDTO,
                                                 @RequestParam(required = false) String module,
                                                 @RequestParam(required = false) String operatorName,
                                                 @RequestParam(required = false) String startDate,
                                                 @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(module)) {
            wrapper.eq(SysOperationLog::getModule, module);
        }
        if (StringUtils.isNotBlank(operatorName)) {
            wrapper.like(SysOperationLog::getOperatorName, operatorName);
        }
        if (StringUtils.isNotBlank(startDate)) {
            wrapper.ge(SysOperationLog::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.isNotBlank(endDate)) {
            wrapper.le(SysOperationLog::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        IPage<SysOperationLog> page = sysOperationLogMapper.selectPage(pageDTO.getPage(), wrapper);
        return Result.success(page);
    }
}
