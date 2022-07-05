package com.kuney.community.application.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.ElasticSearchService;
import com.kuney.community.util.Constants;
import com.kuney.community.util.PageUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * @author kuneychen
 * @since 2022/7/4 12:33
 */
@Controller
@RequestMapping("/search")
@AllArgsConstructor
public class SearchController {

    private ElasticSearchService esService;

    @GetMapping
    public String search(@RequestParam(required = false, defaultValue = "1") int pageNum,
                         String keyword, Model model) throws IOException {
        Page<DiscussPost> page = esService.searchPost(keyword, pageNum, Constants.PAGE_SIZE);
        long[] range = PageUtils.getPageRange(page.getPages(), pageNum);
        model.addAttribute("page", page);
        model.addAttribute("pageBegin", range[0]);
        model.addAttribute("pageEnd", range[1]);
        model.addAttribute("path", "/search?keyword=" + keyword);
        return "site/search";
    }

}
