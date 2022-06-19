package com.kuney.community.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kuney.community.application.entity.DiscussPost;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
public interface DiscussPostMapper extends BaseMapper<DiscussPost> {

    void incrCommentCount(Integer entityId);

}
