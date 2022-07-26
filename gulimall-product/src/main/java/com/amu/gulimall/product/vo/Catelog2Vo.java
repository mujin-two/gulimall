package com.amu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo {

    private String catelog1Id;
    private List<Catelog3Vo> catalog3List;
    private String id;
    private String name;

    /**
     * 三级分类的Vo
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catelog3Vo {
        // 父分类，二级分类Id
        private String catalog2Id;
        private String id;
        private String name;
    }

}
