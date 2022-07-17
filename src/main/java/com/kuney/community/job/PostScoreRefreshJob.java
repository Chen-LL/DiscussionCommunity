package com.kuney.community.job;

import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.application.service.ElasticSearchService;
import com.kuney.community.application.service.LikeService;
import com.kuney.community.util.Constants.EntityType;
import com.kuney.community.util.ObjCheckUtils;
import com.kuney.community.util.RedisKeyUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author kuneychen
 * @since 2022/7/6 21:29
 */
@Component
@AllArgsConstructor
@Slf4j
public class PostScoreRefreshJob implements Job {

    private RedisTemplate<String, Object> redisTemplate;
    private DiscussPostService discussPostService;
    private LikeService likeService;
    private ElasticSearchService esService;

    // 论坛纪元
    private static final LocalDateTime epoch;

    static {
        epoch = LocalDateTime.of(2010, 10, 10, 0, 0, 0);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String key = RedisKeyUtils.getPostScoreKey();
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps(key);
        if (ops.size() == 0) {
            log.info("[任务取消] 没有需要刷新分数的帖子！");
            return;
        }
        log.info("[任务开始] 正在刷新帖子分数，帖子数量：{}", ops.size());
        while (ops.size() > 0) {
            Integer id = (Integer) ops.pop();
            try {
                refreshScore(id);
            } catch (IOException e) {
                log.error("帖子刷新分数失败：[id = {}]", id);
            }
        }
        log.info("[任务结束] 帖子分数刷新完毕！");
    }

    private void refreshScore(Integer postId) throws IOException {
        DiscussPost post = discussPostService.getById(postId);
        if (ObjCheckUtils.isNull(post)) {
            log.error("帖子不存在：[id = {}]", postId);
            return;
        }
        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数
        Integer commentCount = post.getCommentCount();
        // 点赞数
        long likeCount = likeService.likeCount(EntityType.POST, postId);
        // 权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 * likeCount * 2;
        long interval = ChronoUnit.DAYS.between(epoch, post.getCreateTime());
        double score = Math.log10(Math.max(w, 1)) + interval;
        discussPostService.lambdaUpdate()
                .set(DiscussPost::getScore, score)
                .eq(DiscussPost::getId, postId)
                .update();
        post.setScore(score);
        esService.saveOrUpdatePost(post);
    }
}
