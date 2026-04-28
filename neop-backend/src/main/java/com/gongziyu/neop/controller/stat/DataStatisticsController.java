package com.gongziyu.neop.controller.stat;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.annotation.OperationLog;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.DataStatistics;
import com.gongziyu.neop.service.DataStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计接口
 */
@RestController
@RequestMapping("/api/admin/stat")
public class DataStatisticsController {

    @Autowired
    private DataStatisticsService dataStatisticsService;

    @GetMapping("/list")
    @OperationLog(module = "数据统计", description = "查询统计列表")
    public Result<IPage<DataStatistics>> list(PageDTO pageDTO,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate) {
        return Result.success(dataStatisticsService.listPage(pageDTO, startDate, endDate));
    }
}
