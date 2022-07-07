package com.kuney.community.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
public interface CommentService extends IService<Comment> {

    void addComment(Comment comment);

    Page<Comment> getUserCommentPage(int pageNum, int userId);
}
