package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserKeyword implements Serializable {

    private Long id;

    /** 用户 openid */
    private String openid;

    /** 关注的关键词 */
    private String keyword;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
