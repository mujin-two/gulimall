package com.amu.gulimall.product.dao;

import com.amu.gulimall.product.entity.AttrGroupEntity;
import com.amu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-05-05 20:56:11
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
