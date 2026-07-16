package com.tencent.wxcloudrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户关键词响应
 */
@Data
@Schema(description = "用户关注关键词响应")
public class UserKeywordResp {

    @Schema(description = "关键词记录 ID", example = "1")
    private Long id;
    @Schema(description = "关注关键词", example = "猫粮")
    private String keyword;
    @Schema(description = "创建时间，Unix 秒级时间戳", example = "1767225600")
    private Long createdAt;

    public static UserKeywordResp from(com.tencent.wxcloudrun.model.UserKeyword uk) {
        UserKeywordResp resp = new UserKeywordResp();
        resp.setId(uk.getId());
        resp.setKeyword(uk.getKeyword());
        if (uk.getCreatedAt() != null) {
            resp.setCreatedAt(uk.getCreatedAt().atZone(java.time.ZoneId.of("Asia/Shanghai")).toEpochSecond());
        }
        return resp;
    }
}
