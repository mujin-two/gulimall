package com.amu.gulimall.ware.service.impl;

import com.amu.common.constant.WareConstant;
import com.amu.gulimall.ware.entity.PurchaseDetailEntity;
import com.amu.gulimall.ware.service.PurchaseDetailService;
import com.amu.gulimall.ware.service.WareSkuService;
import com.amu.gulimall.ware.vo.MergeVo;
import com.amu.gulimall.ware.vo.PurchaseDoneVo;
import com.amu.gulimall.ware.vo.PurchaseItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.Query;

import com.amu.gulimall.ware.dao.PurchaseDao;
import com.amu.gulimall.ware.entity.PurchaseEntity;
import com.amu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    PurchaseService purchaseService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        // 查询采购单是否已经存在，不存在则创建
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // 只有采购单是新建或是已分配状态才合并
        PurchaseEntity purchaseEntity = purchaseService.getById(purchaseId);
        if (purchaseEntity != null && (purchaseEntity.getStatus() == 0 || purchaseEntity.getStatus() == 1)) {
            List<Long> items = mergeVo.getItems();
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> collect = items.stream().map((i -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setPurchaseId(finalPurchaseId);
                detailEntity.setId(i);
                detailEntity.setStatus(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode());
                return detailEntity;
            })).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);

            PurchaseEntity entity = new PurchaseEntity();
            entity.setId(purchaseId);
            entity.setUpdateTime(new Date());
            this.updateById(entity);
        }
    }

    @Override
    public void purchaseOrderReceive(List<Long> purchaseOrderIds) {
        // 1、确认当前采购单是新建或是已分配状态
        List<PurchaseEntity> collect = purchaseOrderIds.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseEnum.CREATED.getCode()
                    || item.getStatus() == WareConstant.PurchaseEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // 2、改变采购单的状态
        this.updateBatchById(collect);

        // 3、改变采购项的状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(entity.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailEnum.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        });

    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        // 1、改变采购单状态
        Long id = purchaseDoneVo.getId();

        PurchaseEntity purchaseEntity = purchaseService.getById(id);
        if (purchaseEntity == null) {
           return;
        }
        // 2、改变采购项的状态
        List<PurchaseItemVo> items = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> entities = new ArrayList<>();
        boolean error = false;
        for (PurchaseItemVo vo : items) {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setId(vo.getItemId());
            if (vo.getStatus() == WareConstant.PurchaseDetailEnum.HASERROR.getCode()) {
                error = true;
                entity.setStatus(vo.getStatus());
            } else {
                entity.setStatus(WareConstant.PurchaseDetailEnum.FINISH.getCode());
                // 3、将成功采购的入库
                PurchaseDetailEntity byId = purchaseDetailService.getById(vo.getItemId());
                wareSkuService.addStock(byId.getSkuId(), byId.getWareId(),byId.getSkuNum());
            }
            entities.add(entity);
        }
        purchaseDetailService.updateBatchById(entities);
        if (error) {
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.HASERROR.getCode());
        } else {
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.FINISH.getCode());
        }
        purchaseEntity.setUpdateTime(new Date());
        purchaseService.updateById(purchaseEntity);

    }

}