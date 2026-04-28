package com.gongziyu.neop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gongziyu.neop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 扣减库存（乐观锁）
     */
    @Update("UPDATE product SET stock = stock - #{num}, sales = sales + #{num} WHERE id = #{productId} AND stock >= #{num} AND deleted = 0")
    int deductStock(@Param("productId") Long productId, @Param("num") Integer num);

    /**
     * 回补库存（取消订单）
     */
    @Update("UPDATE product SET stock = stock + #{num} WHERE id = #{productId} AND deleted = 0")
    int addStock(@Param("productId") Long productId, @Param("num") Integer num);
}
