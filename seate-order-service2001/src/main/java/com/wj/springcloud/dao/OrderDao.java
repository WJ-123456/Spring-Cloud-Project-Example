package com.wj.springcloud.dao;

import com.wj.springcloud.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderDao {
    // 新增
    void create(Order order);
    // 修改
    void update(@Param("userId") Long userId, @Param("status") Integer status);
}
