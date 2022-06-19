package com.kuney.community.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuney.community.application.entity.Comment;
import com.kuney.community.application.mapper.CommentMapper;
import com.kuney.community.application.mapper.DiscussPostMapper;
import com.kuney.community.application.service.CommentService;
import com.kuney.community.util.Constants.EntityType;
import com.kuney.community.util.HostHolder;
import com.kuney.community.util.SensitiveWordFilter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;

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
}
