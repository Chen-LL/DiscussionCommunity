package com.kuney.community.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuney.community.application.entity.DiscussPost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
public interface DiscussPostService extends IService<DiscussPost> {

    Page<DiscussPost> getIndexPage(Integer pageNum);

    void saveDiscussPost(DiscussPost discussPost);

    Map<String, Object> discussPostDetail(Integer id, Integer pageNum);
}
