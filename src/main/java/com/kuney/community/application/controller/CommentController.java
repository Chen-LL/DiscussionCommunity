package com.kuney.community.application.controller;


import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.application.entity.Comment;
import com.kuney.community.application.service.CommentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @LoginRequired
    @PostMapping("{discussPostId}")
    public String addComment(@PathVariable int discussPostId, Comment comment) {
        commentService.addComment(comment);
        return "redirect:/discuss-post/" + discussPostId;
    }

}

