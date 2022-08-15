package com.amu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.amu.common.to.es.SkuEsModel;
import com.amu.common.utils.R;
import com.amu.gulimall.search.config.ElasticsearchConfig;
import com.amu.gulimall.search.constant.EsConstant;
import com.amu.gulimall.search.feign.ProductFeignService;
import com.amu.gulimall.search.service.MallSearchService;
import com.amu.gulimall.search.vo.AttrResponseVo;
import com.amu.gulimall.search.vo.SearchParam;
import com.amu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService feignService;

    @Override
    public SearchResult search(SearchParam param) {
        //1、动态构建DSL语句
        SearchResult result = null;
        //2、准备检索请求
        SearchRequest searchRequest = builderSearchRequest(param);
        try {
            //3、执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
            //4、封装响应结果
            result = builderSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private SearchRequest builderSearchRequest(SearchParam param) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();// 构建DSL语句

        //1、模糊匹配，过滤
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1关键字匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2.1三级分类id过滤
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2.2品牌id过滤
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2.3属性过滤
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", s[0]));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValues", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }
        //1.2.4库存过滤
        if (param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //1.2.5价格区间过滤
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                } else {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        sourceBuilder.query(boolQuery);

        //2、排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        //3、分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //4、高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }
        //5、聚合分析
        //5.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        //5.1.1品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        //5.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        //5.3 属性聚合 attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // 当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // 当前attr_id对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 当前attr_id对应的所有可能的属性值attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearchResult builderSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        // 封装所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                // 如果有关键字查询就会有高亮信息
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);
        // 当前所有商品涉及的所有属性信息
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 设置属性的id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            // 设置属性的名称
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            // 设置属性的值
            List<String> attrValues = new ArrayList<>();
            List<? extends Terms.Bucket> attr_value_agg = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets();
            for (Terms.Bucket attrValue : attr_value_agg) {
                attrValues.add(attrValue.getKeyAsString());
            }
            attrVo.setAttrValues(attrValues);

            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 当前所有商品涉及的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 品牌id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            // 品牌名称
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            // 品牌图片
            List<? extends Terms.Bucket> brandImgAgg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets();
            if (brandImgAgg != null && brandImgAgg.size() > 0) {
                String brandImg = brandImgAgg.get(0).getKeyAsString();
                brandVo.setBrandImg(brandImg);
            }
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        // 当前所有商品涉及的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 得到分类的Id
            catalogVo.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
            // 得到分类的名字
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        // 分页信息-页码
        result.setPageNum(param.getPageNum());
        // 分页信息-总记录数
        result.setTotal(hits.getTotalHits().value);
        // 分页信息-总页码
        result.setTotalPages((int) (hits.getTotalHits().value + EsConstant.PRODUCT_PAGESIZE - 1) / EsConstant.PRODUCT_PAGESIZE);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 0; i < result.getTotalPages(); i++) {
           pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 构建面包屑导航
        List<SearchResult.NavVo> collect = new ArrayList<>();
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            collect = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = feignService.attrInfo(Long.parseLong(s[0]));

                if (r.getCode() == 0) {
                    Object data = r.get("attr");
                    String dataString = JSON.toJSONString(data);
                    AttrResponseVo attrResponseVo = JSON.parseObject(dataString, new TypeReference<AttrResponseVo>(){});
                    navVo.setNavName(attrResponseVo.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                // TODO 取消面包屑之后将要跳转的位置
                String encode = null;
                try {
                    encode = URLEncoder.encode(attr, "UTF-8");
                    encode = encode.replace("+", "%20");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String link = param.get_queryString().replace("&attrs=" + encode, "");
                navVo.setLink("http://search.gulimall.com/list.html?" + link);

                return navVo;
            }).collect(Collectors.toList());
        }


        result.setNavs(collect);
        return result;
    }
}
