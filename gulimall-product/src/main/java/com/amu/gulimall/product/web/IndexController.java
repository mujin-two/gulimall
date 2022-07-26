package com.amu.gulimall.product.web;

import com.amu.gulimall.product.entity.CategoryEntity;
import com.amu.gulimall.product.service.CategoryService;
import com.amu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model) {
        // 查询所有的一级分类
        List<CategoryEntity> lists = categoryService.getLevel1Categorys();

        model.addAttribute("categories",lists);
        return "index";
    }


    // index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String,List<Catelog2Vo>> getCatelogJson() {
        return categoryService.getCatelogJson();
    }
}
