package com.kuney.community.application.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.util.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Controller
@RequestMapping("/discuss-post")
@AllArgsConstructor
@Slf4j
public class DiscussPostController {

    private DiscussPostService discussPostService;

    @GetMapping
    public String getIndexPage(@RequestParam(required = false, defaultValue = "1") Integer pageNum, Model model) {
        Page<DiscussPost> page = discussPostService.getIndexPage(pageNum);
        long pageEnd = Math.min(page.getPages(), Math.max(5, pageNum + 1));
        long pageBegin = Math.max(Constants.PAGE_NUM, pageEnd - 4);
        model.addAttribute("page", page);
        model.addAttribute("pageBegin", pageBegin);
        model.addAttribute("pageEnd", pageEnd);
        return "index";
    }
}

