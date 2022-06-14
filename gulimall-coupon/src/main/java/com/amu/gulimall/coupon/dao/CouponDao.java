package com.amu.gulimall.coupon.dao;

import com.amu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-06-13 20:45:30
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
