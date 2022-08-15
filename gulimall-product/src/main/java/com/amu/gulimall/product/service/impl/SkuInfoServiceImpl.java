package com.amu.gulimall.product.service.impl;

import com.amu.common.exception.RRException;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.Query;
import com.amu.gulimall.product.dao.SkuInfoDao;
import com.amu.gulimall.product.entity.SkuImagesEntity;
import com.amu.gulimall.product.entity.SkuInfoEntity;
import com.amu.gulimall.product.entity.SpuInfoDescEntity;
import com.amu.gulimall.product.service.*;
import com.amu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.amu.gulimall.product.vo.SkuItemVo;
import com.amu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catalogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price",min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    wrapper.le("price",max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、sku基本信息 pms_sku_info
            SkuInfoEntity info = getById(skuId);
            if (info == null) {
                throw new RRException("无法查询到商品");
            }
            Long spuId = info.getSpuId();
            if (info != null) {
                skuItemVo.setInfo(info);
            }
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3、当前spu的所有销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        },executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4、商品介绍
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDesc);
        },executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            // 5、规格参数信息（属性-属性值）
            List<SpuItemAttrGroupVo> attrGroup = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(),res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroup);
        },executor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            // 2、sku图片信息 pms_sku_images
            List<SkuImagesEntity> imagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(imagesEntities);
        }, executor);

        // 等待所有任务完成
        try {
            CompletableFuture.allOf(saleAttrFuture,descFuture,baseAttrFuture,imagesFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return skuItemVo;
    }


}