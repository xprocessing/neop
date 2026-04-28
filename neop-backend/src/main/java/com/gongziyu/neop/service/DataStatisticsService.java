package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.DataStatistics;

public interface DataStatisticsService extends IService<DataStatistics> {

    /**
     * 分页查询统计数据
     */
    IPage<DataStatistics> listPage(PageDTO pageDTO, String startDate, String endDate);
}
