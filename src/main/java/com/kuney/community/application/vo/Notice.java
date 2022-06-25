package com.kuney.community.application.vo;

import com.kuney.community.application.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuneychen
 * @since 2022/6/25 22:09
 */
@Data
public class Notice {

    private Integer messageId;
    private User from;
    private Integer postId;
    private Integer entityType;
    private LocalDateTime createTime;
    private String topic;

}
