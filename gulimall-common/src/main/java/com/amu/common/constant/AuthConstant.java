package com.amu.common.constant;

import java.util.concurrent.TimeUnit;

public class AuthConstant {
    public static final String SMS_CODE_CACHE_PREFIX = "sms_code:";
    public static final long SMS_CODE_CACHE_EXPIRETIME = 5 * 60 * 1000l;
    public static final TimeUnit SMS_CODE_CACHE_TIMEUNIT = TimeUnit.MILLISECONDS;
}
