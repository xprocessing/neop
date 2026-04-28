package com.gongziyu.neop.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求基类
 */
@Data
public class PageDTO implements Serializable {

    private Integer current = 1;
    private Integer size = 10;

    public <T> IPage<T> getPage() {
        return new Page<>(current, size);
    }
}
