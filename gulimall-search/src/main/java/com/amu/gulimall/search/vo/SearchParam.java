package com.amu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {

    /**
     *  检索关键字
     */
    private String keyword;

    /**
     *  三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件
     *  saleCount_desc
     *  saleCount_asc
     *  skuPrice_desc
     *  skuPrice_asc
     *  hotScore_desc
     *  hotScore_asc
     */
    private String sort;

    /**
     *  过滤条件
     *  hasStock(是否有货)
     *  skuPrice区间
     *  brandId
     *  catalog3Id
     *  attrs
     */
    private Integer hasStock;// 是否有货(默认查询库存)

    private String skuPrice;// 价格区间

    private List<Long> brandId;// 品牌Id（允许多选)

    private List<String> attrs;// 商品属性（多个值用:分隔）

    /**
     *  结果页码
     */
    private Integer pageNum = 1;// 默认第一页

    private String _queryString;// 原生的所有查询条件
}