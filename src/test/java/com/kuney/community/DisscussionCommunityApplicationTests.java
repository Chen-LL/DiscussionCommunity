package com.kuney.community;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DisscussionCommunityApplicationTests {

    @Autowired
    DiscussPostService discussPostService;

    @Test
    void contextLoads() {
        Page<DiscussPost> indexPage = discussPostService.getIndexPage(1);
        for (DiscussPost record : indexPage.getRecords()) {
            log.info("===={}", record);
        }
    }

}
