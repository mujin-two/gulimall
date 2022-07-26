package com.amu.gulimall.product.service.impl;

import com.amu.gulimall.product.service.CategoryBrandRelationService;
import com.amu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.Query;

import com.amu.gulimall.product.dao.CategoryDao;
import com.amu.gulimall.product.entity.CategoryEntity;
import com.amu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1、查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2、组装成树型

        // 2.1、找到一级分类
        List<CategoryEntity> level1Menus = categoryEntities.stream().filter((categoryEntity) ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu,categoryEntities));
            return menu;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查是否被其他地方引用

        // 采用逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList<>();
        // 找它的父id
        while (catelogId != 0) {
            CategoryEntity entity = this.getById(catelogId);
            if (entity == null) {
                break;
            }
            list.add(catelogId);
            catelogId = entity.getParentCid();
        }
        Collections.reverse(list);
        return list.toArray(new Long[0]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid",0));
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {

        // 添加缓存优化
        List<CategoryEntity> selectList = baseMapper.selectList(null);


        // 查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList,0L);
        // 封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1、查到这个一级分类所有对应的二级分类
            List<Catelog2Vo> value = null;
            List<CategoryEntity> entities = getParent_cid(selectList,v.getCatId());
            if (entities != null) {
                value = entities.stream().map(item2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(
                            v.getCatId().toString(), null, item2.getCatId().toString(), item2.getName());
                    // 2、查询二级分类对应的三级分类封装成vo
                    List<CategoryEntity> entities3 = getParent_cid(selectList,item2.getCatId());
                    if (entities3 != null) {
                        List<Catelog2Vo.Catelog3Vo> level3Catelogs = entities3.stream().map(item3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo =
                                    new Catelog2Vo.Catelog3Vo(
                                            item2.getCatId().toString(),item3.getCatId().toString(),item3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(level3Catelogs);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return value;
        }));
        return parent_cid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        return selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
    }

    private List<CategoryEntity> getChildrens(CategoryEntity entity,List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream().filter((categoryEntity) -> categoryEntity.getParentCid() == entity.getCatId())
                .map(categoryEntity -> {
                    // 递归找到子菜单
                    categoryEntity.setChildren(getChildrens(categoryEntity,categoryEntities));
                    return categoryEntity;
                }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
                ).collect(Collectors.toList());
        return collect;
    }
}