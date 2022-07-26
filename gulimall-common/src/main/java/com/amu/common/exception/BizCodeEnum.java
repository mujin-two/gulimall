package com.amu.common.exception;

public enum BizCodeEnum {
    VAILD_EXCEPTION(10001,"参数校验有误"),
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");

    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
