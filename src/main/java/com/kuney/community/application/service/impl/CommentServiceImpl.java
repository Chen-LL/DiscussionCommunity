package com.kuney.community.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuney.community.application.entity.Comment;
import com.kuney.community.application.mapper.CommentMapper;
import com.kuney.community.application.mapper.DiscussPostMapper;
import com.kuney.community.application.service.CommentService;
import com.kuney.community.util.Constants;
import com.kuney.community.util.Constants.EntityType;
import com.kuney.community.util.HostHolder;
import com.kuney.community.util.PageUtils;
import com.kuney.community.util.SensitiveWordFilter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Service
@AllArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private DiscussPostMapper discussPostMapper;
    private SensitiveWordFilter sensitiveWordFilter;
    private HostHolder hostHolder;

    @Transactional
    @Override
    public void addComment(Comment comment) {
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveWordFilter.filter(comment.getContent()));
        comment.setStatus(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUserId(hostHolder.getUser().getId());
        this.save(comment);
        if (EntityType.POST == comment.getEntityType()) {
            discussPostMapper.incrCommentCount(comment.getEntityId());
        }
    }

    @Override
    public Page<Comment> getUserCommentPage(int pageNum, int userId) {
        int current = (pageNum - 1) * Constants.PAGE_SIZE;
        List<Comment> comments = baseMapper.selectComments(current, Constants.PAGE_SIZE, userId);
        Integer total = this.lambdaQuery()
                .ne(Comment::getStatus, 2)
                .eq(Comment::getEntityType, EntityType.POST)
                .eq(Comment::getUserId, userId)
                .count();
        return PageUtils.handle(pageNum, Constants.PAGE_SIZE, total, comments);
    }
}
