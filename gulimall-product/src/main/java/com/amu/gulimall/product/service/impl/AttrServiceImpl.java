package com.amu.gulimall.product.service.impl;

import com.amu.common.constant.ProductConstant;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.Query;
import com.amu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.amu.gulimall.product.dao.AttrDao;
import com.amu.gulimall.product.dao.AttrGroupDao;
import com.amu.gulimall.product.dao.CategoryDao;
import com.amu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.amu.gulimall.product.entity.AttrEntity;
import com.amu.gulimall.product.entity.AttrGroupEntity;
import com.amu.gulimall.product.entity.CategoryEntity;
import com.amu.gulimall.product.service.AttrService;
import com.amu.gulimall.product.service.CategoryService;
import com.amu.gulimall.product.vo.AttrRespVo;
import com.amu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long cateLogId, String attrType) {
        String key = (String) params.get("key");
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        // 设置base查询还是sale 查询
        wrapper.eq("attr_type",
                ProductConstant.AttrEnum.ATTR_TYPE_BASE.getMsg()
                        .equalsIgnoreCase(attrType) ?
                        ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
                        :
                        ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        // 设置属性id
        if (cateLogId != 0) {
            wrapper.eq("catelog_id", cateLogId);
        }

        // 搜索参数不为空添加条件
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_id", key);
                obj.or().like("attr_name", key);
            });
        }
        // 查询数据
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        // 封装数据
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public void saveAttr(AttrVo attr) {
        // 保存基本数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);
        // 保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationDao.insert(relationEntity);
        }
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {

            // 设置分组信息
            AttrAttrgroupRelationEntity relationEntity =
                    relationDao.selectOne(
                            new QueryWrapper<AttrAttrgroupRelationEntity>()
                                    .eq("attr_id", attrId));
            if (relationEntity != null) {
                attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrId());
                if (attrGroupEntity != null) {
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        // 设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        attrRespVo.setCatelogPath(categoryService.findCatelogPath(catelogId));
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {

            // 修改所属分组关联表
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attr.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationDao.update(relationEntity, new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
        }
    }

    @Override
    public List<AttrEntity> queryRelation(List<AttrAttrgroupRelationEntity> relationEntityList) {
        List<AttrEntity> attrEntities = new ArrayList<>();
        if (relationEntityList != null && relationEntityList.size() > 0) {
            List<Long> attrIds = relationEntityList.stream().map((attr) -> attr.getAttrId()).collect(Collectors.toList());
            attrEntities = (List<AttrEntity>) this.listByIds(attrIds);
        }
        return attrEntities;
    }

    // 获取当前分组没有关联的所有属性
    @Override
    public PageUtils queryNoRelation(Map<String, Object> params, Long attrGroupId) {
        // 1、当前分组只能关联自己所属分类里面的属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2、只能关联没有被其他分组关联的属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());

        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        if (groupEntities != null && groupEntities.size() > 0) {
            List<Long> collect = groupEntities.stream().map(item -> item.getAttrGroupId()).collect(Collectors.toList());
            List<Long> attrIds = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect)).stream().map(item -> item.getAttrId()).collect(Collectors.toList());
            if (attrIds != null && attrIds.size() > 0) {
                wrapper.notIn("attr_id",attrIds);
            }
        }


        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id",key);
                w.or().like("attr_group_id",key);
            });
        }
        PageUtils pageUtils = new PageUtils(this.page(new Query<AttrEntity>().getPage(params), wrapper));
        return pageUtils;
    }




}