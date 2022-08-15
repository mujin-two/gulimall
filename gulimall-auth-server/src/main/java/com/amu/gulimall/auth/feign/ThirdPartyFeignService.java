package com.amu.gulimall.auth.feign;

import com.amu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendCode")
     R sendCode(@RequestParam("phoneNumber") String phoneNumber, @RequestParam("code") String code);

}
