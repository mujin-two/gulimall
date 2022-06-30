package com.amu.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo{
    /*
     * 所属分类名称
     */
    private String catelogName;

    /*
     * 所属分组名称
     */
    private String groupName;

    /*
        所属分类
     */
    private Long[] catelogPath;
}
