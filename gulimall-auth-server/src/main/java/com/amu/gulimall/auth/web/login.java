package com.amu.gulimall.auth.web;

import com.amu.common.exception.BizCodeEnum;
import com.amu.common.constant.AuthConstant;
import com.amu.common.exception.RRException;
import com.amu.common.utils.R;
import com.amu.gulimall.auth.feign.MemberFeignService;
import com.amu.gulimall.auth.feign.ThirdPartyFeignService;
import com.amu.gulimall.auth.vo.UserLoginVo;
import com.amu.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class login {



    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @RequestMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phoneNumber") String phoneNumber) {

        if (StringUtils.isEmpty(phoneNumber)) {
            throw new RRException("号码不存在");
        }
        // TODO 接口防刷

        // 1、5分钟内同一手机号只能发送一次验证码
        String beforeCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phoneNumber);
        if (!StringUtils.isEmpty(beforeCode)) {
            long beforeCodeTime = Long.parseLong(beforeCode.split("_")[1]);
            if (System.currentTimeMillis() - beforeCodeTime < AuthConstant.SMS_CODE_CACHE_EXPIRETIME) {
                return R.error(BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMessage());
            }
        }

        // 生成验证码
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int num = (int) (Math.random() * 10);
            code.append(num);
        }
        // 保存验证码至redis，有过期时间
        // redis_key : sms_code:{phoneNumber}
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phoneNumber,code+ "_" + System.currentTimeMillis(),
                AuthConstant.SMS_CODE_CACHE_EXPIRETIME, AuthConstant.SMS_CODE_CACHE_TIMEUNIT);
        thirdPartyFeignService.sendCode(phoneNumber,code.toString());
        return R.ok();
    }

    @PostMapping("/register")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // 校验出错，转发到注册页
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.
                    toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 调用注册
        // 校验验证码
        String code = vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(redisCode) && code.equals(redisCode.split("_")[0])) {
            // 删除验证码
            redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
            // 执行注册
            R regist = memberFeignService.regist(vo);
            if (regist.getCode() == 0) {
                // 注册成功返回登录页
                return "redirect:http://auth.gulimall.com/login.html";
            } else {
                // 注册失败
                Map<String,String> errors = new HashMap<>();
                errors.put("msg",(String) regist.get("msg"));
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            return "redirect:http://auth.gulimall.com/reg.html";
        }


    }

    @PostMapping("/login")
    public String login(UserLoginVo vo,RedirectAttributes redirectAttributes) {
        // 登录
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            return "redirect:http://gulimall.com";
        } else {
            Map<String,String> errros = new HashMap<>();
            errros.put("msg",(String) login.get("msg"));
            redirectAttributes.addFlashAttribute("errors",errros);
            return "redirect:http://auth.gulimall.com";
        }
    }

}
