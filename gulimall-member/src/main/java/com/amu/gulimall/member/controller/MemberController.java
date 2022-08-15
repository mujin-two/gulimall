package com.amu.gulimall.member.controller;

import com.amu.common.exception.BizCodeEnum;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.R;
import com.amu.gulimall.member.entity.MemberEntity;
import com.amu.gulimall.member.exception.PhoneExistException;
import com.amu.gulimall.member.exception.UserNameExistException;
import com.amu.gulimall.member.service.MemberService;
import com.amu.gulimall.member.vo.MemberLoginVo;
import com.amu.gulimall.member.vo.MemberRegistVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-06-13 22:07:23
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;


    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo) {
        try {
            memberService.regist(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        return memberService.login(vo);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
