package com.kuney.community.application.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuney.community.application.entity.Message;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.mapper.MessageMapper;
import com.kuney.community.application.service.MessageService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.application.vo.Notice;
import com.kuney.community.exception.CustomException;
import com.kuney.community.util.Constants;
import com.kuney.community.util.Constants.KafkaTopic;
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
    public Map<String, Object> getMessageIndex(int pageNum, int userId) {
        HashMap<String, Object> result = new HashMap<>();
        // 会话列表
        List<Message> messageList = messageMapper.selectConversations((pageNum - 1) * PAGE_SIZE, PAGE_SIZE, userId);
        // 总会话数
        int conversationCount = messageMapper.countConversation(userId);
        Page<Message> messagePage = PageUtils.handle(pageNum, PAGE_SIZE, conversationCount, messageList);

        for (Message message : messageList) {
            message.setFrom(getLetterTarget(message.getConversationId()));
            Integer count = this.lambdaQuery()
                    .eq(Message::getConversationId, message.getConversationId())
                    .eq(Message::getToId, userId)
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
            message.setFrom(userService.getUser(message.getFromId()));
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
            return userService.getUser(id0);
        }
        return userService.getUser(id1);
    }

    @Override
    public Map<String, Object> getNoticeList(Integer userId) {
        HashMap<String, Object> data = new HashMap<>();

        // 评论类通知
        HashMap<String, Object> commentNotice = getNotice(userId, KafkaTopic.COMMENT);
        data.put("commentNotice", commentNotice);
        // 点赞类通知
        HashMap<String, Object> likeNotice = getNotice(userId, KafkaTopic.LIKE);
        data.put("likeNotice", likeNotice);
        // 关注类通知
        HashMap<String, Object> followNotice = getNotice(userId, KafkaTopic.FOLLOW);
        data.put("followNotice", followNotice);

        return data;
    }

    @Override
    public int countUnreadNotice(Integer userId) {
        return this.lambdaQuery()
                .eq(Message::getFromId, Constants.SYSTEM_USER_ID)
                .eq(Message::getToId, userId)
                .eq(Message::getStatus, 0)
                .count();
    }

    @Override
    public int countUnreadLetter(Integer userId) {
        return this.lambdaQuery()
                .ne(Message::getFromId, Constants.SYSTEM_USER_ID)
                .eq(Message::getToId, userId)
                .eq(Message::getStatus, 0)
                .count();
    }

    @Override
    public Map<String, Object> getNoticeDetail(String topic, int pageNum, int userId) {
        HashMap<String, Object> result = new HashMap<>();
        Page<Message> page = this.lambdaQuery()
                .ne(Message::getStatus, 2)
                .eq(Message::getConversationId, topic)
                .eq(Message::getFromId, Constants.SYSTEM_USER_ID)
                .eq(Message::getToId, userId)
                .orderByDesc(Message::getId)
                .page(new Page<>(pageNum, PAGE_SIZE));
        List<Message> messageList = page.getRecords();
        if (ObjCheckUtils.nonEmpty(messageList)) {
            List<Notice> records = messageList.stream()
                    .map(message -> {
                        HashMap<String, Object> content = JSONObject.parseObject(message.getContent(), HashMap.class);
                        Notice notice = new Notice();
                        notice.setMessageId(message.getId());
                        notice.setCreateTime(message.getCreateTime());
                        notice.setTopic(topic);
                        notice.setFrom(userService.getUser((Integer) content.get("userId")));
                        notice.setEntityType((Integer) content.get("entityType"));
                        if (!KafkaTopic.FOLLOW.equals(topic)) {
                            notice.setPostId((Integer) content.get("postId"));
                        }
                        return notice;
                    }).collect(Collectors.toList());
            List<Integer> ids = records.stream().map(Notice::getMessageId).collect(Collectors.toList());
            this.lambdaUpdate().set(Message::getStatus, 1).in(Message::getId, ids).update();
            result.put("page", PageUtils.handle(pageNum, PAGE_SIZE, page.getTotal(), records));
        } else {
            result.put("page", page);
        }
        long[] range = PageUtils.getPageRange(page.getPages(), pageNum);
        result.put("pageBegin", range[0]);
        result.put("pageEnd", range[1]);
        return result;
    }

    private HashMap<String, Object> getNotice(Integer userId, String topic) {
        HashMap<String, Object> noticeOfTopic = null;

        LambdaQueryWrapper<Message> queryWrapper = Wrappers.<Message>lambdaQuery()
                .eq(Message::getFromId, Constants.SYSTEM_USER_ID)
                .eq(Message::getToId, userId)
                .eq(Message::getConversationId, topic)
                .ne(Message::getStatus, 2);

        Message message = this.getOne(queryWrapper.orderByDesc(Message::getId).last("limit 1"));
        if (message != null) {
            noticeOfTopic = new HashMap<>();

            message.setLetters(this.count(queryWrapper));
            HashMap<String, Object> content = JSONObject.parseObject(message.getContent(), HashMap.class);
            message.setFrom(userService.getUser((Integer) content.get("userId")));
            int unread = this.count(queryWrapper.eq(Message::getStatus, 0));
            message.setUnread(unread);

            noticeOfTopic.put("message", message);
            noticeOfTopic.put("entityType", content.get("entityType"));
            noticeOfTopic.put("entityId", content.get("entityId"));
            if (!KafkaTopic.FOLLOW.equals(topic)) {
                noticeOfTopic.put("postId", content.get("postId"));
            }
        }
        return noticeOfTopic;
    }

}
