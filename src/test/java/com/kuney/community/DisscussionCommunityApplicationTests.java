package com.kuney.community;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.Comment;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.util.CommunityUtils;
import com.kuney.community.util.EncodeUtils;
import com.kuney.community.util.SensitiveWordFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@SpringBootTest
class DisscussionCommunityApplicationTests {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private CommunityUtils communityUtils;
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void contextLoads() {
        Page<DiscussPost> indexPage = discussPostService.getIndexPage(1);
        for (DiscussPost record : indexPage.getRecords()) {
            log.info("===={}", record);
        }
    }

    @Test
    void testSendEmail() {
        String to = "2267519670@qq.com";
        String subject = "TEST";
        Context context = new Context();
        context.setVariable("username", to);
        String url = "http://localhost:8080/community/activation";
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        communityUtils.sendSimpleMail(to, subject, content);
    }


    @Autowired
    SensitiveWordFilter sensitiveWordFilter;

    @Test
    void testSensitiveFilter() {
        String text = "※aa※bca※a※b※bqqqqc※c※";
        String text2 = sensitiveWordFilter.filter(text);
        log.info("过滤前：{}", text);
        log.info("过滤后：{}", text2);
    }

    @Test
    void testPostDetail() {
        long begin = System.currentTimeMillis();
        Map<String, Object> data = discussPostService.discussPostDetail(275, 1);
        long end = System.currentTimeMillis();
        log.info("------版本2耗时：{}ms", end - begin);
        // 版本1耗时：818ms
        // 版本2耗时：689ms
        log.info("帖子：{}", data.get("discussPost"));
        log.info("=========================================");
        Page<Comment> page = (Page<Comment>) data.get("commentPage");
        int no = 1;
        for (Comment comment : page.getRecords()) {
            log.info("父评论{}：{}", no++, comment);
            int j = 1;
            if (comment.getReplyList() != null) {
                for (Comment child : comment.getReplyList()) {
                    log.info("子评论{}：{}", j++, child);
                }
            }
            log.info("----------------------------------");
        }

    }

    @Test
    void encodePassword() {
        String password = EncodeUtils.encodePassword("123", "a09113");
        log.info("-------------{}", password);
    }

}
