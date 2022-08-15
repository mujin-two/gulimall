package com.amu.common.exception;

public enum BizCodeEnum {
    VAILD_EXCEPTION(10001,"参数校验有误"),
    VALID_SMS_CODE_EXCEPTION(10002,"验证码获取频率太高，请稍后再试"),
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户已存在"),
    USER_OR_PASSWORD_EXCEPTION(15002,"用户名或密码错误"),
    PHONE_EXIST_EXCEPTION(15003,"手机号已存在");

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
