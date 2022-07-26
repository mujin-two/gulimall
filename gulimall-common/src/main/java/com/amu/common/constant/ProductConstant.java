package com.amu.common.constant;


public class ProductConstant {
    public enum AttrEnum {
        ATTR_TYPE_BASE(1,"base"),ATTR_TYPE_SALE(0,"sale");
        private int code;
        private String msg;
        AttrEnum (int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    // 商品的状态，新建、上架、下架
    public enum StatusEnum {
        NEW_SPU(0,"新建"),
        SPU_UP(1,"商品上架"),
        SPU_DOWN(2,"商品下架");

        private int code;
        private String msg;

        StatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
