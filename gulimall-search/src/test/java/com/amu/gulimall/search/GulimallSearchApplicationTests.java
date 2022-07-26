package com.amu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.amu.gulimall.search.config.ElasticsearchConfig;
import lombok.Data;
import lombok.ToString;
import net.minidev.json.JSONValue;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    RestHighLevelClient client;

    @Data
    @ToString
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Test
    public void contextLoads() throws Exception{
        IndexRequest index = new IndexRequest("users");
        index.id("1");
//        index.source("username","zhangsan","age",18,"gender","male");
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(18);
        user.setGender("male");
        String jsonString = JSONValue.toJSONString(user);
        index.source(jsonString, XContentType.JSON);
        IndexResponse result = client.index(index, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(result);
    }

    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void searchDate() throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // 指定DSL，检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构造检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        searchSourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"));

        searchRequest.source(searchSourceBuilder);

        // 检索
        SearchResponse searchResponse = client.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(searchResponse.toString());

        // 结果处理
        // 获取所有查询的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit: searchHits
             ) {
            String sourceAsString = hit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }
        // 获取检索的聚合信息
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            System.out.println("年龄" + bucket.getKeyAsString() + ",人数:" + bucket.getDocCount());
        }

        Avg balanceAvg = aggregations.get("balanceAvg");
        System.out.println("平均薪资:" + balanceAvg.getValue());
    }

}
