package com.amu.gulimall.product.feign;

import com.amu.common.to.es.SkuEsModel;
import com.amu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeigbService {

    /*
    上架商品
 */
    @PostMapping("/search/product/save")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList);
}
