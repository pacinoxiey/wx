package com.tencent.wxcloudrun.dto;

import lombok.Data;

/**
 * 用户关键词响应
 */
@Data
public class UserKeywordResp {

    private Long id;
    private String keyword;
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
