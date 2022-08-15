package com.amu.gulimall.product.service;

import com.amu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.amu.gulimall.product.vo.AttrGroupWithAttrVo;
import com.amu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.amu.common.utils.PageUtils;
import com.amu.gulimall.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-05-05 20:56:11
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    void saveRelation(AttrAttrgroupRelationEntity[] entities);

    List<AttrGroupWithAttrVo> listAttrGroupWithAttrsByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

