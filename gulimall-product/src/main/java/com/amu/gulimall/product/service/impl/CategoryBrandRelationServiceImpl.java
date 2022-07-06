package com.amu.gulimall.product.service.impl;

import com.amu.gulimall.product.dao.BrandDao;
import com.amu.gulimall.product.dao.CategoryDao;
import com.amu.gulimall.product.entity.BrandEntity;
import com.amu.gulimall.product.entity.CategoryEntity;
import com.amu.gulimall.product.vo.CategoryBrandRelationVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.Query;

import com.amu.gulimall.product.dao.CategoryBrandRelationDao;
import com.amu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.amu.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelation.setBrandName(
                brandDao.selectById(categoryBrandRelation.getBrandId()).getName());
        categoryBrandRelation.setCatelogName(
                categoryDao.selectById(categoryBrandRelation.getCatelogId()).getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        this.update(categoryBrandRelationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId,name);
    }

    @Override
    public List<CategoryBrandRelationEntity> listBrands(Long catId) {
        return this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    }

}