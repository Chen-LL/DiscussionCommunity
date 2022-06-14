package com.kuney.community.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuney.community.application.entity.Comment;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.mapper.DiscussPostMapper;
import com.kuney.community.application.service.CommentService;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.exception.CustomException;
import com.kuney.community.util.*;
import com.kuney.community.util.Constants.EntityType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Service
@AllArgsConstructor
@Slf4j
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService {

    private UserService userService;
    private SensitiveWordFilter sensitiveWordFilter;
    private HostHolder hostHolder;
    private CommentService commentService;

    @SuppressWarnings("unchecked")
    @Override
    public Page<DiscussPost> getIndexPage(Integer pageNum) {
        Page<DiscussPost> page = this.lambdaQuery()
                .ne(DiscussPost::getStatus, 2)
                .orderByDesc(DiscussPost::getType, DiscussPost::getStatus, DiscussPost::getCreateTime)
                .page(new Page<>(pageNum, Constants.PAGE_SIZE));
        List<DiscussPost> discussPosts = page.getRecords();
        if (ObjCheckUtils.nonEmpty(discussPosts)) {
            Set<Integer> userIds = discussPosts.stream().map(DiscussPost::getUserId).collect(Collectors.toSet());
            List<User> users = userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
            discussPosts.forEach(discussPost -> discussPost.setUser(userMap.get(discussPost.getUserId())));
        }
        return page;
    }

    @Override
    public void saveDiscussPost(DiscussPost discussPost) {
        String title = HtmlUtils.htmlEscape(discussPost.getTitle());
        String content = HtmlUtils.htmlEscape(discussPost.getContent());
        discussPost.setTitle(sensitiveWordFilter.filter(title));
        discussPost.setContent(sensitiveWordFilter.filter(content));
        discussPost.setUserId(hostHolder.getUser().getId());
        discussPost.setCreateTime(LocalDateTime.now());
        this.save(discussPost);
    }

    @Override
    public Map<String, Object> discussPostDetail(Integer id, Integer pageNum) {
        HashMap<String, Object> data = new HashMap<>();

        DiscussPost discussPost = this.getById(id);
        if (ObjCheckUtils.isNull(discussPost)) {
            throw new CustomException(404, "帖子不存在");
        }
        discussPost.setUser(userService.getById(discussPost.getUserId()));
        Page<Comment> commentPage = pageComment(pageNum, discussPost.getId());
        long[] range = PageUtils.getPageRange(commentPage.getPages(), pageNum);

        data.put("discussPost", discussPost);
        data.put("commentPage", commentPage);
        data.put("pageBegin", range[0]);
        data.put("pageEnd", range[1]);
        return data;
    }

    /**
     * 获取帖子的评论列表（版本1）
     *
     * @param pageNum       当前页
     * @param discussPostId 帖子id
     * @return
     */
    private Page<Comment> pageComment1(Integer pageNum, Integer discussPostId) {
        Page<Comment> commentPage = commentService.lambdaQuery()
                .eq(Comment::getEntityType, EntityType.POST)
                .eq(Comment::getEntityId, discussPostId)
                .orderByAsc(Comment::getCreateTime)
                .page(new Page<>(pageNum, Constants.PAGE_SIZE));

        // 回帖列表：对帖子的评论
        List<Comment> comments = commentPage.getRecords();
        for (Comment comment : comments) {
            // 回复列表：对评论的评论
            List<Comment> replyList = commentService.lambdaQuery()
                    .eq(Comment::getEntityType, EntityType.COMMENT)
                    .eq(Comment::getEntityId, comment.getId())
                    .orderByAsc(Comment::getCreateTime)
                    .list();
            comment.setUser(userService.getById(comment.getUserId()));
            comment.setReplyList(replyList);
            for (Comment reply : replyList) {
                reply.setUser(userService.getById(reply.getUserId()));
                if (reply.getTargetId() != 0) {
                    reply.setTargetUser(userService.getById(reply.getTargetId()));
                }
            }
        }
        return commentPage;
    }

    /**
     * 获取帖子的评论列表（版本2）
     * 将循环查询数据库优化为查询一遍数据库，在内存中组装评论间的关系
     * @param pageNum 当前页
     * @param discussPostId 帖子id
     * @return
     */
    private Page<Comment> pageComment(Integer pageNum, Integer discussPostId) {

        @AllArgsConstructor
        @EqualsAndHashCode
        class CommentKey {
            private int entityId;
            private int entityType;
        }

        Page<Comment> commentPage = commentService.lambdaQuery()
                .eq(Comment::getEntityType, EntityType.POST)
                .eq(Comment::getEntityId, discussPostId)
                .orderByAsc(Comment::getCreateTime)
                .page(new Page<>(pageNum, Constants.PAGE_SIZE));

        // 回帖列表：对帖子的评论
        List<Comment> comments = commentPage.getRecords();
        if (ObjCheckUtils.nonEmpty(comments)) {
            List<Integer> commentIds = new ArrayList<>();
            for (Comment comment : comments) {
                commentIds.add(comment.getId());
                comment.setUser(userService.getById(comment.getUserId()));
            }
            // 回帖列表对应所有回复
            List<Comment> allReplyList = commentService.lambdaQuery()
                    .eq(Comment::getEntityType, EntityType.COMMENT)
                    .in(Comment::getEntityId, commentIds)
                    .orderByAsc(Comment::getCreateTime)
                    .list();
            if (ObjCheckUtils.nonEmpty(allReplyList)) {
                // 将回复列表分组，key：实体id+实体类型，value：回复列表
                Map<CommentKey, List<Comment>> map = new HashMap<>();
                for (Comment reply : allReplyList) {
                    CommentKey key = new CommentKey(reply.getEntityId(), reply.getEntityType());
                    if (!map.containsKey(key)) {
                        map.put(key, new ArrayList<>());
                    }
                    map.get(key).add(reply);
                    reply.setUser(userService.getById(reply.getUserId()));
                    if (reply.getTargetId() != 0) {
                        reply.setTargetUser(userService.getById(reply.getTargetId()));
                    }
                }
                for (Comment comment : comments) {
                    CommentKey key = new CommentKey(comment.getId(), EntityType.COMMENT);
                    comment.setReplyList(map.get(key));
                }
            }
        }
        return commentPage;
    }
}
