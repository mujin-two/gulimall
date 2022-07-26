package com.amu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.amu.common.to.es.SkuEsModel;
import com.amu.gulimall.search.config.ElasticsearchConfig;
import com.amu.gulimall.search.constant.EsConstant;
import com.amu.gulimall.search.service.ProductSaveService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service("ProductSaveService")
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException {
        // 1、在es中建立为一个索引，建立映射关系
        // 2、给es中保存数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel esModel : skuEsModelList) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(esModel.getSkuId().toString());
            String s = JSON.toJSONString(esModel);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticsearchConfig.COMMON_OPTIONS);

        // TODO 错误的批量处理
        return bulk.hasFailures();

    }
}
