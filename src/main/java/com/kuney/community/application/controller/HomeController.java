package com.kuney.community.application.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.util.PageUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author kuneychen
 * @since 2022/6/13 21:10
 */
@Controller
@RequestMapping("index")
@AllArgsConstructor
public class HomeController {

    private DiscussPostService discussPostService;

    @GetMapping
    public String getIndexPage(@RequestParam(required = false, defaultValue = "1") int pageNum,
                               @RequestParam(required = false, defaultValue = "0") int isHot,
                               Model model) {
        Page<DiscussPost> page = discussPostService.getPostPage(pageNum, isHot);
        long[] range = PageUtils.getPageRange(page.getPages(), pageNum);
        model.addAttribute("page", page);
        model.addAttribute("pageBegin", range[0]);
        model.addAttribute("pageEnd", range[1]);
        model.addAttribute("isHot", isHot);
        model.addAttribute("path", "/index");
        return "home";
    }

}
