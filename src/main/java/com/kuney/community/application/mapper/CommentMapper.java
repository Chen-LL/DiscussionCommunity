package com.kuney.community.application.mapper;

import com.kuney.community.application.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
public interface CommentMapper extends BaseMapper<Comment> {

    List<Comment> selectComments(@Param("current") int current,
                                 @Param("limit") int limit,
                                 @Param("userId") int userId);
}
