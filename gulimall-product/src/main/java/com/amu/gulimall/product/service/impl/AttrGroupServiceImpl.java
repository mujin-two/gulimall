package com.amu.gulimall.product.service.impl;

import com.amu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.amu.gulimall.product.dao.AttrDao;
import com.amu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.amu.gulimall.product.entity.AttrEntity;
import com.amu.gulimall.product.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.Query;

import com.amu.gulimall.product.dao.AttrGroupDao;
import com.amu.gulimall.product.entity.AttrGroupEntity;
import com.amu.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (catelogId != 0L) {
            wrapper.eq("catelog_id", catelogId);
        }
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key);
                obj.or().like("attr_group_name", key);
            });
        }
        return new PageUtils(this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        ));
    }

    @Override
    public void saveRelation(AttrAttrgroupRelationEntity[] entities) {
        if (entities != null && entities.length > 0) {
            for (AttrAttrgroupRelationEntity relationEntity : entities) {
                Long attrId = relationEntity.getAttrId();
                AttrAttrgroupRelationEntity entity = relationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
                if (entity != null) {
                    relationDao.update(relationEntity,
                            new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrId));
                } else {
                    relationDao.insert(relationEntity);
                }
            }
        }
    }
}