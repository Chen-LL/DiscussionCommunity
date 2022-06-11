package com.kuney.community.application.service.impl;

import com.kuney.community.application.entity.Comment;
import com.kuney.community.application.mapper.CommentMapper;
import com.kuney.community.application.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

}
