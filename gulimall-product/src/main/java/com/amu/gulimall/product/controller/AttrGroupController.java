package com.amu.gulimall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.amu.gulimall.product.entity.AttrEntity;
import com.amu.gulimall.product.service.AttrAttrgroupRelationService;
import com.amu.gulimall.product.service.AttrService;
import com.amu.gulimall.product.service.CategoryService;
import com.amu.gulimall.product.vo.AttrGroupWithAttrVo;
import com.amu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.amu.gulimall.product.entity.AttrGroupEntity;
import com.amu.gulimall.product.service.AttrGroupService;
import com.amu.common.utils.PageUtils;
import com.amu.common.utils.R;
import org.w3c.dom.Attr;


/**
 * 属性分组
 *
 * @author amu
 * @email mujinmj@gmail.com
 * @date 2022-06-13 17:28:16
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(params);

        return R.ok().put("page", page);
    }


    @Transactional
    @GetMapping("/{attrGroupId}/attr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrAttrgroupRelationEntity> lists = relationService.listById(attrGroupId);
        List<AttrEntity> page = attrService.queryRelation(lists);
        return R.ok().put("data", page);
    }

    // 查询所有未关联属性
    @GetMapping("/{attrGroupId}/noattr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R listNoattr(@RequestParam Map<String, Object> params,
                        @PathVariable("attrGroupId") Long attrGroupId){
        PageUtils page = attrService.queryNoRelation(params,attrGroupId);

        return R.ok().put("page", page);
    }

    /**
     *  获取分类下所有分组和关联属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R attrList(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = attrGroupService.listAttrGroupWithAttrsByCatelogId(catelogId);

        return R.ok().put("data",attrGroupWithAttrVos);
    }

    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R listRelation(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryPage(params,catelogId);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}/{catelogId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId,
                  @PathVariable("catelogId") Long catelogId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		if (attrGroup == null) {
		    attrGroup = new AttrGroupEntity();
        }

		attrGroup.setCatelogPath(categoryService.findCatelogPath(catelogId));

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }
    /*
     * 增加关联关系
     */
    @PostMapping("/attr/relation")
    public R saveRelation(@RequestBody AttrAttrgroupRelationEntity[] entities) {
        attrGroupService.saveRelation(entities);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 删除关联关系
     */
    @RequestMapping("/attr/relation/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R deleteRelation(@RequestBody AttrAttrgroupRelationEntity[] relationEntities){
//        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
        relationService.delete(relationEntities);
        return R.ok();
    }
}
