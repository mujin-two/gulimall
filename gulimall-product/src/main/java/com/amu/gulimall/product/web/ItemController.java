package com.amu.gulimall.product.web;

import com.amu.gulimall.product.service.SkuInfoService;
import com.amu.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 获取当前sku详情
     * @return
     */
    @GetMapping("/item/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        SkuItemVo itemVo = skuInfoService.item(skuId);
        model.addAttribute("item",itemVo);
        return "item";
    }
}
