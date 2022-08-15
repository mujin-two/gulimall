package com.amu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.amu.gulimall.product.service.CategoryBrandRelationService;
import com.amu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
import org.springframework.util.StringUtils;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;


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
            menu.setChildren(getChildrens(menu, categoryEntities));
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
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    // 当前方法使用缓存，如果缓存有则直接从缓存中查询并返回，并不执行方法，如果缓存没有，执行方法后再将结果放入缓存
    @Cacheable(value = {"category"},key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        // 先从缓存中查询
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            // 缓存中没有就从数据库中查询
            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatalogJsonFromRedisWithLock();

            return catelogJsonFromDb;
        }
        return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    // 分布式锁
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromRedisWithLock() {
        // 分布式锁id
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            // 加锁成功
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                // LUA脚本
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                        Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            // 自旋
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromRedisWithLock();
        }
    }

    // redisson分布式锁
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromRedisWithRedissonLock() {
        RLock lock = redissonClient.getLock("catalog-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    // 从数据库查询并封装，本地锁
    public synchronized Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {

        // 查询缓存
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }

        // 查到数据后加入缓存
        Map<String, List<Catelog2Vo>> parent_cid = getDataFromDb();
        String catalogJson = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJSON", catalogJson, 6, TimeUnit.HOURS);
        return parent_cid;
    }


    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        // 封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1、查到这个一级分类所有对应的二级分类
            List<Catelog2Vo> value = null;
            List<CategoryEntity> entities = getParent_cid(selectList, v.getCatId());
            if (entities != null) {
                value = entities.stream().map(item2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(
                            v.getCatId().toString(), null, item2.getCatId().toString(), item2.getName());
                    // 2、查询二级分类对应的三级分类封装成vo
                    List<CategoryEntity> entities3 = getParent_cid(selectList, item2.getCatId());
                    if (entities3 != null) {
                        List<Catelog2Vo.Catelog3Vo> level3Catelogs = entities3.stream().map(item3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo =
                                    new Catelog2Vo.Catelog3Vo(item2.getCatId().toString(), item3.getCatId().toString(), item3.getName());
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

    private List<CategoryEntity> getChildrens(CategoryEntity entity, List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream().filter((categoryEntity) -> categoryEntity.getParentCid() == entity.getCatId())
                .map(categoryEntity -> {
                    // 递归找到子菜单
                    categoryEntity.setChildren(getChildrens(categoryEntity, categoryEntities));
                    return categoryEntity;
                }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
                ).collect(Collectors.toList());
        return collect;
    }
}