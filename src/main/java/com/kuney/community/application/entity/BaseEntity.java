package com.kuney.community.application.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuneychen
 * @since 2022/6/24 21:56
 */
@Data
public class BaseEntity {

    private LocalDateTime createTime;
}
