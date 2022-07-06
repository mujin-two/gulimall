package com.amu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PurchaseItemVo {
    @NotNull
    private Long itemId;
    private Integer status;
    private String reason;
}
