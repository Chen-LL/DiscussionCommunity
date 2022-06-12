package com.kuney.community;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.util.CommunityUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

}
