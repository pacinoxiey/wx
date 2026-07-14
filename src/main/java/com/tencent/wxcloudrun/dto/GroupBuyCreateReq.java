package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class GroupBuyCreateReq {

    public enum Type {
        LINK,
        TOKEN
    }

    /** A PDD link or token, selected by type. */
    private Type type;

    /** 用户粘贴的拼团原始文本 */
    private String rawText;

    /** 是否强制新建 (已存在相同拼团时，true=强制新建，false=返回已有) */
    private Boolean force;
}
