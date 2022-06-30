package com.amu.gulimall.product;

import com.amu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.amu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    AttrAttrgroupRelationDao relationDao;
    @Test
    public void contextLoads() {
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(1L);
        relationEntity.setAttrGroupId(3L);
        relationDao.updateById(relationEntity);
    }
}
