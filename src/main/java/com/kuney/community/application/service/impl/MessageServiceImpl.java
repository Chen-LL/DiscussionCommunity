package com.kuney.community.application.service.impl;

import com.kuney.community.application.entity.Message;
import com.kuney.community.application.mapper.MessageMapper;
import com.kuney.community.application.service.MessageService;
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
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

}
