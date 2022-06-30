package com.amu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.amu.common.utils.PageUtils;
import com.amu.gulimall.product.entity.AttrAttrgroupRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-05-05 20:56:12
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void delete(AttrAttrgroupRelationEntity[] relationEntities);

    List<AttrAttrgroupRelationEntity> listById(Long attrGroupId);
}

