package com.amu.gulimall.ware.service;

import com.amu.common.to.SkuHasStockVo;
import com.amu.common.utils.PageUtils;
import com.amu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-06-13 22:21:56
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> hasStock(List<Long> skuIds);
}

