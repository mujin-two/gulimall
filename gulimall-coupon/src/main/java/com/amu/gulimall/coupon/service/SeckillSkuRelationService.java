package com.amu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.amu.common.utils.PageUtils;
import com.amu.gulimall.coupon.entity.SeckillSkuRelationEntity;

import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-06-13 20:45:30
 */
public interface SeckillSkuRelationService extends IService<SeckillSkuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

