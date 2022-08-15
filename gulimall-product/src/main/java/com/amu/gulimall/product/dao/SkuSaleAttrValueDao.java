package com.amu.gulimall.product.dao;

import com.amu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.amu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-05-05 20:56:11
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);
}
