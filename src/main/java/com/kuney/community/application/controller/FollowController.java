package com.kuney.community.application.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.FollowService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.Constants.EntityType;
import com.kuney.community.util.HostHolder;
import com.kuney.community.util.PageUtils;
import com.kuney.community.util.Result;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author kuneychen
 * @since 2022/6/20 21:31
 */
@RequestMapping("follow")
@Controller
@AllArgsConstructor
public class FollowController {

    private FollowService followService;
    private HostHolder hostHolder;
    private UserService userService;

    @LoginRequired
    @PostMapping
    @ResponseBody
    public Result follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        long count = followService.followerCount(EntityType.USER, entityId);
        return Result.data(count);
    }

    @LoginRequired
    @PostMapping("undo")
    @ResponseBody
    public Result unFollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(), entityType, entityId);
        long count = followService.followerCount(EntityType.USER, entityId);
        return Result.data(count);
    }

    @GetMapping("{userId}/followee")
    public String followeeList(@PathVariable int userId, Model model,
                               @RequestParam(required = false, defaultValue = "1") int pageNum) {
        Page<Map<String, Object>> page = followService.followeePage(userId, pageNum);
        User user = userService.getById(userId);
        long[] range = PageUtils.getPageRange(page.getPages(), pageNum);

        model.addAttribute("page", page);
        model.addAttribute("user", user);
        model.addAttribute("pageBegin", range[0]);
        model.addAttribute("pageEnd", range[1]);
        model.addAttribute("path", String.format("/follow/%d/followee", userId));
        return "site/followee";
    }

    @GetMapping("{userId}/follower")
    public String followerList(@PathVariable int userId, Model model,
                               @RequestParam(required = false, defaultValue = "1") int pageNum) {
        Page<Map<String, Object>> page = followService.followerPage(userId, pageNum);
        User user = userService.getById(userId);
        long[] range = PageUtils.getPageRange(page.getPages(), pageNum);

        model.addAttribute("page", page);
        model.addAttribute("user", user);
        model.addAttribute("pageBegin", range[0]);
        model.addAttribute("pageEnd", range[1]);
        model.addAttribute("path", String.format("/follow/%d/follower", userId));
        return "site/follower";
    }

}
