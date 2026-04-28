package com.gongziyu.neop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.DataStatistics;
import com.gongziyu.neop.mapper.DataStatisticsMapper;
import com.gongziyu.neop.service.DataStatisticsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DataStatisticsServiceImpl extends ServiceImpl<DataStatisticsMapper, DataStatistics> implements DataStatisticsService {

    @Override
    public IPage<DataStatistics> listPage(PageDTO pageDTO, String startDate, String endDate) {
        LambdaQueryWrapper<DataStatistics> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(startDate)) {
            wrapper.ge(DataStatistics::getStatDate, startDate);
        }
        if (StringUtils.isNotBlank(endDate)) {
            wrapper.le(DataStatistics::getStatDate, endDate);
        }
        wrapper.orderByDesc(DataStatistics::getStatDate);
        return baseMapper.selectPage(pageDTO.getPage(), wrapper);
    }
}
