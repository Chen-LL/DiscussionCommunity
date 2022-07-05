package com.kuney.community.application.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;
import com.kuney.community.application.service.ElasticSearchService;
import com.kuney.community.application.service.LikeService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.Constants.EntityType;
import com.kuney.community.util.PageUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kuneychen
 * @since 2022/7/4 12:36
 */
@Service
@AllArgsConstructor
@Slf4j
public class ElasticSearchServiceImpl implements ElasticSearchService {

    private ElasticsearchClient esClient;
    private LikeService likeService;
    private UserService userService;

    @Override
    public Page<DiscussPost> searchPost(String keyword, int pageNum, int pageSize) throws IOException {
        SearchResponse<DiscussPost> response = esClient.search(req -> req
                .index("discusspost")
                .from((pageNum - 1) * pageSize).size(pageSize)
                .query(q -> q
                        .multiMatch(m -> m
                                .fields("title", "content")
                                .query(keyword)
                                .analyzer("ik_smart")
                        )
                ).highlight(h -> h
                        .preTags("<em>").postTags("</em>")
                        .fields("title", new HighlightField.Builder().build())
                        .fields("content", new HighlightField.Builder().build())
                ), DiscussPost.class
        );
        long total = response.hits().total().value();
        List<Hit<DiscussPost>> hitList = response.hits().hits();
        List<DiscussPost> postList = new ArrayList<>();
        for (Hit<DiscussPost> postHit : hitList) {
            DiscussPost post = postHit.source();
            if (postHit.highlight().containsKey("title")) {
                post.setTitle(postHit.highlight().get("title").get(0));
            }
            if (postHit.highlight().containsKey("content")) {
                post.setContent(postHit.highlight().get("content").get(0));
            } else {
                post.setContent("");
            }
            post.setUser(userService.getUser(post.getUserId()));
            post.setLikeCount(likeService.likeCount(EntityType.POST, post.getId()));
            postList.add(post);
        }
        return PageUtils.handle(pageNum, pageSize, total, postList);
    }

    @Override
    public void saveOrUpdatePost(DiscussPost post) throws IOException {
        esClient.index(i -> i
                .index("discusspost")
                .id(post.getId().toString())
                .document(post)
        );
    }
}
