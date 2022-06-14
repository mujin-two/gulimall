package com.amu.gulimall.product.dao;

import com.amu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-05-05 20:56:11
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
