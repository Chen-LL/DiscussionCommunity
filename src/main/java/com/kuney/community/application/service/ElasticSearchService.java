package com.kuney.community.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;

import java.io.IOException;

/**
 * @author kuneychen
 * @since 2022/7/4 12:36
 */
public interface ElasticSearchService {
    Page<DiscussPost> searchPost(String keyword, int pageNum, int pageSize) throws IOException;

    void saveOrUpdatePost(DiscussPost post) throws IOException;
}
