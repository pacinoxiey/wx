package com.tencent.wxcloudrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "拼团搜索请求")
public class GroupBuySearchReq {

    /** 用户手写的搜索关键词（作为一个整体模糊匹配） */
    @Schema(description = "用户输入的搜索关键词，作为一个整体进行模糊匹配", example = "猫粮")
    private String keyword;

    /** 预制标签，多个用 & 分隔，每个单独模糊匹配 */
    @Schema(description = "预置标签表达式，多个标签使用 & 分隔，每个标签单独模糊匹配", example = "猫粮&鲜肉")
    private String tags;

    /** 是否隐藏已过期，默认 true */
    @Schema(description = "是否隐藏已过期拼团", example = "true")
    private Boolean hideExpired = true;

    /** 页码, 从1开始 */
    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer page = 1;

    /** 每页条数 */
    @Schema(description = "每页条数", example = "20")
    private Integer pageSize = 20;
}
