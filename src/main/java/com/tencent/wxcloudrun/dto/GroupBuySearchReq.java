package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class GroupBuySearchReq {

    /** 用户手写的搜索关键词（作为一个整体模糊匹配） */
    private String keyword;

    /** 预制标签，多个用 & 分隔，每个单独模糊匹配 */
    private String tags;

    /** 页码, 从1开始 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer pageSize = 20;
}
