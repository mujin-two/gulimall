package com.amu.gulimall.thirdparty.controller;

import com.amu.common.utils.R;
import com.amu.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsSendController {


    @Autowired
    SmsComponent smsComponent;

    /**
     *  提供发送验证码功能
     * @param phoneNumber
     * @param code
     * @return
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phoneNumber") String phoneNumber, @RequestParam("code") String code) {
        smsComponent.sendSmsCode(phoneNumber,code);
        return R.ok();
    }
}
