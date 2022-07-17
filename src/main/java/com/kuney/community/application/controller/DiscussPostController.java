package com.kuney.community.application.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.annotation.Permission;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.event.Event;
import com.kuney.community.event.EventProducer;
import com.kuney.community.util.Constants.EntityType;
import com.kuney.community.util.Constants.KafkaTopic;
import com.kuney.community.util.Constants.Role;
import com.kuney.community.util.PageUtils;
import com.kuney.community.util.RedisKeyUtils;
import com.kuney.community.util.Result;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 前端控制器
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
    private EventProducer eventProducer;
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping
    @ResponseBody
    @LoginRequired
    public Result publish(@Validated DiscussPost discussPost) {
        discussPostService.saveDiscussPost(discussPost);

        Event event = new Event();
        event.setTopic(KafkaTopic.PUBLISH);
        event.setUserId(discussPost.getUserId());
        event.setEntityType(EntityType.POST);
        event.setEntityId(discussPost.getId());
        eventProducer.sendMessage(event);

        redisTemplate.opsForSet().add(RedisKeyUtils.getPostScoreKey(), discussPost.getId());
        return Result.success("发布成功！");
    }

    @GetMapping("{id}")
    public String detail(@PathVariable Integer id, Model model,
                         @RequestParam(required = false, defaultValue = "1") Integer pageNum) {
        Map<String, Object> data = discussPostService.discussPostDetail(id, pageNum);
        model.addAttribute("post", data.get("discussPost"));
        model.addAttribute("page", data.get("commentPage"));
        model.addAttribute("pageBegin", data.get("pageBegin"));
        model.addAttribute("pageEnd", data.get("pageEnd"));
        model.addAttribute("path", "/discuss-post/" + id);
        return "site/discuss-detail";
    }

    @Permission(role = Role.MODERATOR)
    @LoginRequired
    @PostMapping("{id}/type/{type}")
    @ResponseBody
    public Result setType(@PathVariable("id") int postId, @PathVariable("type") int type) {
        discussPostService.lambdaUpdate()
                .set(DiscussPost::getType, type)
                .eq(DiscussPost::getId, postId)
                .update();
        return Result.success();
    }

    @Permission
    @LoginRequired
    @PostMapping("{id}/status/{status}")
    @ResponseBody
    public Result setStatus(@PathVariable("id") int postId, @PathVariable("status") int status) {
        discussPostService.setStatus(postId, status);

        String topic = status == 2 ? KafkaTopic.DELETE : KafkaTopic.UPDATE;
        Event event = new Event();
        event.setTopic(topic);
        event.setEntityType(EntityType.POST);
        event.setEntityId(postId);
        eventProducer.sendMessage(event);

        redisTemplate.opsForSet().add(RedisKeyUtils.getPostScoreKey(), postId);
        return Result.success();
    }

    @GetMapping("user/{userId}")
    public String getUserPostPage(@RequestParam(required = false, defaultValue = "1") int pageNum,
                                  @PathVariable int userId, Model model) {
        Page<DiscussPost> page = discussPostService.getUserPostPage(pageNum, userId);
        long[] range = PageUtils.getPageRange(page.getPages(), pageNum);
        model.addAttribute("page", page);
        model.addAttribute("userId", userId);
        model.addAttribute("pageBegin", range[0]);
        model.addAttribute("pageEnd", range[1]);
        model.addAttribute("path", "/discuss-post/user/" + userId);
        return "site/my-post";
    }

}

