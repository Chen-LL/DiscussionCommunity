package com.kuney.community;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.Comment;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.DiscussPostService;
import com.kuney.community.util.CommunityUtils;
import com.kuney.community.util.EncodeUtils;
import com.kuney.community.util.SensitiveWordFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<DiscussPost> postList = discussPostService.lambdaQuery().like(DiscussPost::getTitle, "因特网").list();
        postList.forEach(p -> p.setScore(Math.random() * 1000));
        discussPostService.updateBatchById(postList);
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

    @Autowired
    private ElasticsearchClient client;

    @Data
    // @NoArgsConstructor
    // @AllArgsConstructor
    static class Student {
        private Integer id;
        private String name;
        private String sex;
    }

    @Test
    void testEsIndex() {
        Student student = new Student();
        student.setId(0);
        student.setName("666");
        student.setSex("666");
        IndexResponse response = null;
        try {
            response = client.index(i -> {
                return i.index("student").id(student.getId().toString()).document(student);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("-----response: {}", response);
    }

    @Test
    void testEsBulk() {
        List<DiscussPost> postList = discussPostService.list();
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (DiscussPost post : postList) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index("discusspost")
                            .id(post.getId().toString())
                            .document(post)
                    )
            );
        }
        try {
            client.bulk(br.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testEsGet() {
        try {
            GetResponse<DiscussPost> response = client.get(g -> g.index("discusspost").id("109"), DiscussPost.class);
            DiscussPost post = response.source();
            log.info("post: {}", post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testEsSearch() {
        try {
            SearchResponse<DiscussPost> response = client.search(req -> req
                            .from(0).size(10)
                            .index("discusspost")
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .fields("title", "content")
                                            .query("新人报道")
                                            .analyzer("ik_smart")
                                    )
                            ).highlight(h -> h
                                    .preTags("<b>").postTags("</b>")
                                    .fields("title", new HighlightField.Builder().build())
                                    .fields("content", new HighlightField.Builder().build())
                            ).sort(s -> s.field(f -> f.field("score").order(SortOrder.Desc)))
                            .sort(s -> s.field(f -> f.field("createTime").order(SortOrder.Desc))),
                    DiscussPost.class
            );
            long total = response.hits().total().value();
            List<Hit<DiscussPost>> hitList = response.hits().hits();
            log.info("resp: {}", response);
            for (Hit<DiscussPost> postHit : hitList) {
                Map<String, List<String>> map = postHit.highlight();
                log.info("{}-{}", "title", map.get("title"));
                log.info("{}-{}", "content", map.get("content"));
                log.info("sort-{}", postHit.sort());
            }
            List<DiscussPost> postList = hitList.stream().map(Hit::source).collect(Collectors.toList());
            for (int i = 0; i < postList.size(); i++) {
                log.info("post-{}：{}", i + 1, postList.get(i));
            }
            log.info("total: {}", total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testUpdate() {
        DiscussPost post = discussPostService.getById(139);
        post.setCommentCount(111);
        try {
            IndexResponse response = client.index(i -> i
                    .index("discusspost")
                    .id("139")
                    .document(post)
            );
            log.info("response: {}", response.result().jsonValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
