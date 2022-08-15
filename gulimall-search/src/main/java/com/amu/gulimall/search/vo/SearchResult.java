package com.amu.gulimall.search.vo;

import com.amu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *  商品查询结果的封装对象
 */
@Data
public class SearchResult {
    // 查询出的所有商品信息
    private List<SkuEsModel> products;

    //========分页信息===========
    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    private List<Integer> pageNavs;

    //=========商品信息===========


    // 面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    /**
     * 当前查询结果中所有涉及到的品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询结果中所有涉及到的分类
     */
    private List<CatalogVo> catalogs;

    /**
     * 当前查询结果中所有涉及到的属性
     */
    private List<AttrVo> attrs;

    //=========以上是返回给页面的所有信息============
    /**
     * 返回的品牌的封装
     */
    @Data
    public static class BrandVo{
        /**
         * 品牌Id
         */
        private Long brandId;

        /**
         * 品牌名称
         */
        private String brandName;

        /**
         * 品牌图片地址
         */
        private String brandImg;
    }

    /**
     * 返回的分类的封装
     */
    @Data
    public static class CatalogVo {
        /**
         * 分类Id
         */
        private Long catalogId;

        /**
         * 分类名称
         */
        private String catalogName;
    }

    /**
     * 返回的属性的封装
     */
    @Data
    public static class AttrVo {
        /**
         * 属性Id
         */
        private Long attrId;

        /**
         * 属性名称
         */
        private String attrName;

        /**
         * 属性的值
         */
        private List<String> attrValues;
    }
}
