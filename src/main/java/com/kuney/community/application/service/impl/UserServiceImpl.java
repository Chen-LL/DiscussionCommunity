package com.kuney.community.application.service.impl;

import com.kuney.community.application.entity.User;
import com.kuney.community.application.mapper.UserMapper;
import com.kuney.community.application.service.UserService;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
