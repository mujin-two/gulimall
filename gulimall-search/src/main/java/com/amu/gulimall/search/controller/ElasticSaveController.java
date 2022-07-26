package com.amu.gulimall.search.controller;

import com.amu.common.exception.BizCodeEnum;
import com.amu.common.to.es.SkuEsModel;
import com.amu.common.utils.R;
import com.amu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("search")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    /*
        上架商品
     */
    @PostMapping("/product/save")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        try {
            productSaveService.productStatusUp(skuEsModelList);
        } catch (Exception e) {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }

        return R.ok();
    }
}
