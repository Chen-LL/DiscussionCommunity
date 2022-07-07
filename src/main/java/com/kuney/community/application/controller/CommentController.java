package com.kuney.community.application.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.application.entity.Comment;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.CommentService;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.event.Event;
import com.kuney.community.event.EventProducer;
import com.kuney.community.util.Constants.EntityType;
import com.kuney.community.util.Constants.KafkaTopic;
import com.kuney.community.util.PageUtils;
import com.kuney.community.util.RedisKeyUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Controller
@RequestMapping("/comment")
@AllArgsConstructor
public class CommentController {

    private CommentService commentService;
    private EventProducer eventProducer;
    private DiscussPostService discussPostService;
    private RedisTemplate<String, Object> redisTemplate;

    @LoginRequired
    @PostMapping("{postId}")
    public String addComment(@PathVariable int postId, @Validated Comment comment) {
        commentService.addComment(comment);

        Event event = new Event();
        event.setTopic(KafkaTopic.COMMENT);
        event.setUserId(comment.getUserId());
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId());
        event.setData("postId", postId);

        if (EntityType.POST == comment.getEntityType()) {
            DiscussPost discussPost = discussPostService.getById(postId);
            event.setEntityUserId(discussPost.getUserId());
        } else if (EntityType.COMMENT == comment.getEntityType()) {
            Comment target = commentService.getById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        // 发送评论通知
        eventProducer.sendMessage(event);

        // 发送更新es文档的消息
        if (EntityType.POST == comment.getEntityType()) {
            event = new Event();
            event.setTopic(KafkaTopic.UPDATE);
            event.setEntityId(postId);
            event.setEntityType(EntityType.POST);
            event.setUserId(comment.getUserId());
            eventProducer.sendMessage(event);

            redisTemplate.opsForSet().add(RedisKeyUtils.getPostScoreKey(), postId);
        }
        return "redirect:/discuss-post/" + postId;
    }

    @GetMapping("user/{userId}")
    public String getUserCommentPage(@RequestParam(required = false, defaultValue = "1") int pageNum,
                                     @PathVariable int userId, Model model) {
        Page<Comment> page = commentService.getUserCommentPage(pageNum, userId);
        long[] range = PageUtils.getPageRange(page.getPages(), pageNum);
        model.addAttribute("page", page);
        model.addAttribute("pageBegin", range[0]);
        model.addAttribute("pageEnd", range[1]);
        model.addAttribute("path", "/comment/user/" + userId);
        return "site/my-reply";
    }

}

