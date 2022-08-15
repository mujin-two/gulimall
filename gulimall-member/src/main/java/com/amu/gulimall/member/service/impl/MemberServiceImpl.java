package com.amu.gulimall.member.service.impl;

import com.amu.common.exception.BizCodeEnum;
import com.amu.common.utils.R;
import com.amu.gulimall.member.dao.MemberLevelDao;
import com.amu.gulimall.member.entity.MemberLevelEntity;
import com.amu.gulimall.member.exception.PhoneExistException;
import com.amu.gulimall.member.exception.UserNameExistException;
import com.amu.gulimall.member.vo.MemberLoginVo;
import com.amu.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.Query;

import com.amu.gulimall.member.dao.MemberDao;
import com.amu.gulimall.member.entity.MemberEntity;
import com.amu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {


    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());

        // 检查手机号和用户名是否唯一
        checkUserNameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhone());

        // 设置手机
        memberEntity.setMobile(vo.getPhone());
        // 设置用户名
        memberEntity.setUsername(vo.getUserName());
        // 设置密码
        // 密码要进行加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(bCryptPasswordEncoder.encode(vo.getPassword()));

        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException{
        Integer username = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer mobile = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public R login(MemberLoginVo vo) {
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", vo.getLoginacct())
                .or().eq("mobile", vo.getLoginacct()));
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (memberEntity != null && passwordEncoder.matches(vo.getPassword(),memberEntity.getPassword())) {
            // TODO 单点登录
            return R.ok();
        }
        return R.error(BizCodeEnum.USER_OR_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.USER_OR_PASSWORD_EXCEPTION.getMessage());
    }
}