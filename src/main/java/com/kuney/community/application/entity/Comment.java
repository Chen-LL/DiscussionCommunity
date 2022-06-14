package com.kuney.community.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    /**
     * 评论所对应的实体类型：1-帖子，2-评论
     */
    private Integer entityType;

    /**
     * 实体对应的id：帖子/评论
     */
    private Integer entityId;

    /**
     * 评论指向的用户id，默认值0：不指向用户
     */
    private Integer targetId;

    private String content;

    private Integer status;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<Comment> replyList;

    @TableField(exist = false)
    private User user, targetUser;

}
