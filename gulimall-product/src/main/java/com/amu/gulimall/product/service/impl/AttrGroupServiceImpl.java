package com.amu.gulimall.product.service.impl;

import com.amu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.amu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.amu.gulimall.product.entity.AttrEntity;
import com.amu.gulimall.product.service.AttrAttrgroupRelationService;
import com.amu.gulimall.product.service.AttrService;
import com.amu.gulimall.product.vo.AttrGroupWithAttrVo;
import com.amu.gulimall.product.vo.AttrVo;
import com.amu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    AttrAttrgroupRelationService relationService;

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

    @Override
    public List<AttrGroupWithAttrVo> listAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 查询当前分类下的所有分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        // 封装当前分组下的所有关联属性
        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = groupEntities.stream().map((item) -> {
            AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrVo);

            // 查询所有当前分组下的分组列表
            List<AttrAttrgroupRelationEntity> relationEntities = relationService.listById(item.getAttrGroupId());
            List<AttrVo> attrs = new ArrayList<>();
            if (relationEntities != null) {
                List<AttrEntity> attrEntities = attrService.queryRelation(relationEntities);
                attrs = attrEntities.stream().map((attr) -> {
                    AttrVo attrVo = new AttrVo();
                    BeanUtils.copyProperties(attr, attrVo);
                    attrVo.setAttrGroupId(item.getAttrGroupId());
                    return attrVo;
                }).collect(Collectors.toList());
            }
            attrGroupWithAttrVo.setAttrs(attrs);
            return attrGroupWithAttrVo;
        }).collect(Collectors.toList());
        return attrGroupWithAttrVos;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 1、查出当前spu
        AttrGroupDao baseMapper = this.getBaseMapper();
        List<SpuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;
    }
}