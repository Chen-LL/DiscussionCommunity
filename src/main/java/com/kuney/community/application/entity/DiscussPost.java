package com.kuney.community.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

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
public class DiscussPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    @NotBlank(message = "标题为空")
    private String title;

    @NotBlank(message = "内容为空")
    private String content;

    /**
     * 0-普通; 1-置顶;
     */
    private Integer type;

    /**
     * 0-正常; 1-精华; 2-拉黑;
     */
    private Integer status;

    private LocalDateTime createTime;

    private Integer commentCount;

    private Double score;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private long likeCount;

    @TableField(exist = false)
    private int likeStatus;
}
