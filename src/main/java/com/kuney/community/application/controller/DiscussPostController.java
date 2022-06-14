package com.kuney.community.application.controller;


import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.util.HostHolder;
import com.kuney.community.util.Result;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Controller
@RequestMapping("discuss-post")
@AllArgsConstructor
public class DiscussPostController {

    private DiscussPostService discussPostService;
    private HostHolder hostHolder;

    @PostMapping
    @ResponseBody
    public Result publish(@Validated DiscussPost discussPost) {
        if (hostHolder.getUser() == null) {
            return Result.fail("请先登录！");
        }
        discussPostService.saveDiscussPost(discussPost);
        return Result.success("发布成功！");
    }

    @GetMapping("{id}")
    public String detail(@PathVariable Integer id, Model model,
                         @RequestParam(required = false, defaultValue = "1") Integer pageNum) {
        Map<String, Object> data = discussPostService.discussPostDetail(id, pageNum);
        model.addAttribute("discussPost", data.get("discussPost"));
        model.addAttribute("page", data.get("commentPage"));
        model.addAttribute("pageBegin", data.get("pageBegin"));
        model.addAttribute("pageEnd", data.get("pageEnd"));
        return "site/discuss-detail";
    }
}

