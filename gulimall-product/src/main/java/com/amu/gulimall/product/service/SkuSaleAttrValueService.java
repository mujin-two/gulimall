package com.amu.gulimall.product.service;

import com.amu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.amu.common.utils.PageUtils;
import com.amu.gulimall.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-05-05 20:56:11
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);
}

