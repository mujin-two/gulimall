package com.amu.gulimall.product;

import com.amu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.amu.gulimall.product.dao.AttrGroupDao;
import com.amu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.amu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.amu.gulimall.product.service.SkuInfoService;
import com.amu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    StringRedisTemplate template;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Autowired
    SkuInfoService skuInfoService;

    @Test
    public void contextLoads() {
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(1L);
        relationEntity.setAttrGroupId(3L);
        relationDao.updateById(relationEntity);
    }

    @Test
    public void redisTest() {
//        ValueOperations<String, String> ops = template.opsForValue();
//        ops.set("hello","world_" + UUID.randomUUID().toString());

//        System.out.println(ops.get("k1"));

        System.out.println(redissonClient.toString());
    }

    @Test
    public void testAttrGroupDao() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(1L, 225L);
        for (SpuItemAttrGroupVo spuItemAttrGroupVo : attrGroupWithAttrsBySpuId) {
            System.out.println(spuItemAttrGroupVo);
        }
    }

    @Test
    public void testSkuSaleAttrValueDao() {
        System.out.println(skuSaleAttrValueDao.getSaleAttrsBySpuId(1L));
    }

    @Test
    public void testSkuInfoService() {
        System.out.println(skuInfoService.getById(1L));
    }
}
