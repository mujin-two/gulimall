package com.amu.gulimall.member.service;

import com.amu.common.utils.R;
import com.amu.gulimall.member.exception.PhoneExistException;
import com.amu.gulimall.member.exception.UserNameExistException;
import com.amu.gulimall.member.vo.MemberLoginVo;
import com.amu.gulimall.member.vo.MemberRegistVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.amu.common.utils.PageUtils;
import com.amu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-06-13 22:07:23
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkUserNameUnique(String userName) throws PhoneExistException;

    void checkPhoneUnique(String phone) throws UserNameExistException;

    R login(MemberLoginVo vo);
}

