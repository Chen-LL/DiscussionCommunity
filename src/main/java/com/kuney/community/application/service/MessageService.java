package com.kuney.community.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuney.community.application.entity.Message;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
public interface MessageService extends IService<Message> {

    Map<String, Object> getMessageIndex(int pageNum, int userId);


    Map<String, Object> letterDetailPage(String conversationId, int pageNum);

    void saveMessage(String username, String content);

    Map<String, Object> getNoticeList(Integer userId);

    int countUnreadNotice(Integer userId);

    int countUnreadLetter(Integer userId);

    Map<String, Object> getNoticeDetail(String topic, int pageNum, int userId);
}
