package com.kuney.community.event;

import com.alibaba.fastjson.JSONObject;
import com.kuney.community.application.entity.Message;
import com.kuney.community.application.service.MessageService;
import com.kuney.community.util.Constants;
import com.kuney.community.util.ObjCheckUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kuneychen
 * @since 2022/6/22 18:17
 */
@Slf4j
@Component
public class EventConsumer implements Constants.KafkaTopic {

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {LIKE, COMMENT, FOLLOW})
    public void handleEvent(ConsumerRecord<String, String> record) {
        if (record == null || ObjCheckUtils.isBlank(record.value())) {
            log.error("消息内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            log.error("消息格式错误！");
            return;
        }
        Message message = new Message();
        message.setFromId(Constants.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(LocalDateTime.now());

        Map<String, Object> content = new HashMap<>(event.getData());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        content.put("userId", event.getUserId());
        message.setContent(JSONObject.toJSONString(content));

        messageService.save(message);
    }

}
