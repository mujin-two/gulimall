package com.amu.gulimall.search.service;

import com.amu.gulimall.search.vo.SearchResult;
import com.amu.gulimall.search.vo.SearchParam;


public interface MallSearchService {
    /**
     *  查询所有符合条件的商品
     * @param param 检索的所有参数的封装
     * @return 检索结果，包含页面需要的所有信息
     */
    SearchResult search(SearchParam param);

}
