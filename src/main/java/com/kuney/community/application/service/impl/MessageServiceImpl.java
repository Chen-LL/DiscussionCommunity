package com.kuney.community.application.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuney.community.application.entity.Message;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.mapper.MessageMapper;
import com.kuney.community.application.service.MessageService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.exception.CustomException;
import com.kuney.community.util.HostHolder;
import com.kuney.community.util.ObjCheckUtils;
import com.kuney.community.util.PageUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kuney.community.util.Constants.PAGE_SIZE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Service
@AllArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {


    private HostHolder hostHolder;
    private MessageMapper messageMapper;
    private UserService userService;

    @Override
    public Map<String, Object> getMessageIndex(int pageNum) {
        HashMap<String, Object> result = new HashMap<>();
        User user = hostHolder.getUser();
        // 总未读数
        Integer unreadCount = this.lambdaQuery()
                .eq(Message::getToId, user.getId())
                .eq(Message::getStatus, 0)
                .ne(Message::getFromId, 1)
                .count();

        // 会话列表
        List<Message> messageList = messageMapper.selectConversations((pageNum - 1) * PAGE_SIZE, PAGE_SIZE, user.getId());
        // 总会话数
        int conversationCount = messageMapper.countConversation(user.getId());
        Page<Message> messagePage = PageUtils.handle(pageNum, PAGE_SIZE, conversationCount, messageList);

        for (Message message : messageList) {
            message.setFrom(getLetterTarget(message.getConversationId()));
            Integer count = this.lambdaQuery()
                    .eq(Message::getConversationId, message.getConversationId())
                    .eq(Message::getToId, user.getId())
                    .eq(Message::getStatus, 0)
                    .count();
            message.setUnread(count);
            Integer letters = this.lambdaQuery()
                    .eq(Message::getConversationId, message.getConversationId())
                    .ne(Message::getStatus, 2)
                    .count();
            message.setLetters(letters);
        }
        long[] range = PageUtils.getPageRange(messagePage.getPages(), pageNum);

        result.put("unreadCount", unreadCount);
        result.put("page", messagePage);
        result.put("pageBegin", range[0]);
        result.put("pageEnd", range[1]);
        return result;
    }

    @Override
    public Map<String, Object> letterDetailPage(String conversationId, int pageNum) {
        HashMap<String, Object> result = new HashMap<>();
        User user = hostHolder.getUser();

        Page<Message> page = this.lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .ne(Message::getStatus, 2)
                .orderByDesc(Message::getId)
                .page(new Page<>(pageNum, PAGE_SIZE));
        List<Message> letters = page.getRecords();
        for (Message message : letters) {
            message.setFrom(userService.getById(message.getFromId()));
        }

        // 设置已读
        if (ObjCheckUtils.nonEmpty(letters)) {
            List<Integer> ids = letters.stream()
                    .filter(letter -> letter.getToId().equals(user.getId()) && letter.getStatus() == 0)
                    .map(Message::getId)
                    .collect(Collectors.toList());
            if (ObjCheckUtils.nonEmpty(ids)) {
                this.lambdaUpdate().set(Message::getStatus, 1).in(Message::getId, ids).update();
            }
        }

        User target = getLetterTarget(conversationId);
        long[] range = PageUtils.getPageRange(page.getPages(), pageNum);

        result.put("page", page);
        result.put("pageBegin", range[0]);
        result.put("pageEnd", range[1]);
        result.put("target", target);
        return result;
    }

    @Override
    public void saveMessage(String username, String content) {
        User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (ObjCheckUtils.isNull(user)) {
            throw new CustomException(400, "用户不存在");
        }
        Integer fromId = hostHolder.getUser().getId();
        Integer toId = user.getId();
        Message message = new Message();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setConversationId(buildConversationId(fromId, toId));
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(LocalDateTime.now());
        this.save(message);
    }

    private String buildConversationId(Integer fromId, Integer toId) {
        if (fromId < toId) {
            return fromId + "_" + toId;
        }
        return toId + "_" + fromId;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]), id1 = Integer.parseInt(ids[1]);
        User user = hostHolder.getUser();
        if (id0 != user.getId()) {
            return userService.getById(id0);
        }
        return userService.getById(id1);
    }

}
