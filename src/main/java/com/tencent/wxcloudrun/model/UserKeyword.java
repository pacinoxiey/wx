package com.tencent.wxcloudrun.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户关注关键词记录")
public class UserKeyword implements Serializable {

    @Schema(description = "关键词记录 ID", example = "1")
    private Long id;

    /** 用户 openid */
    @Schema(description = "微信用户 openid")
    private String openid;

    /** 关注的关键词 */
    @Schema(description = "关注关键词", example = "猫粮")
    private String keyword;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
