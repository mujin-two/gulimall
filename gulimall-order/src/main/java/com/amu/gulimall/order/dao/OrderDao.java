package com.amu.gulimall.order.dao;

import com.amu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-06-13 22:16:26
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
