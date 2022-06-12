package com.kuney.community.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.mapper.DiscussPostMapper;
import com.kuney.community.application.service.DiscussPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.Constants;
import com.kuney.community.util.ObjCheckUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService {

    private UserService userService;

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
}
