package com.amu.gulimall.ware.service;

import com.amu.gulimall.ware.vo.MergeVo;
import com.amu.gulimall.ware.vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.amu.common.utils.PageUtils;
import com.amu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-06-13 22:21:56
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void purchaseOrderReceive(List<Long> purchaseOrderIds);

    void done(PurchaseDoneVo purchaseDoneVo);
}

