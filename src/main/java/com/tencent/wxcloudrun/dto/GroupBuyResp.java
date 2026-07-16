package com.tencent.wxcloudrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "拼团信息响应")
public class GroupBuyResp {

    @Schema(description = "拼团 ID", example = "1001")
    private Long id;
    @Schema(description = "拼团链接或任务链接")
    private String link;
    @Schema(description = "输入来源类型：LINK=链接，TOKEN=口令")
    private String inputType;

    /** 来源平台 */
    @Schema(description = "来源平台，例如：拼多多、京东", example = "拼多多")
    private String platform;

    /** 商品名称 */
    @Schema(description = "商品名称", example = "鲜肉猫粮 2kg")
    private String productName;

    /** 拼团价格 */
    @Schema(description = "拼团价格", example = "29.90")
    private BigDecimal groupPrice;

    /** 剩余名额 */
    @Schema(description = "剩余参团名额", example = "2")
    private Integer remainingSlots;

    /** 商品图片路径 */
    @Schema(description = "商品图片地址")
    private String imageUrl;

    /** 原始链接 */
    @Schema(description = "原始分享链接")
    private String shareUrl;

    /** 状态: 1=进行中 2=已过期 */
    @Schema(description = "拼团状态：1=进行中，2=已过期", example = "1")
    private Integer status;

    /** 过期时间 (Unix时间戳，秒) */
    @Schema(description = "过期时间，Unix 秒级时间戳", example = "1767225600")
    private Long expireTime;

    /** 倒计时描述 (如"剩余18小时32分") */
    @Schema(description = "倒计时展示文案", example = "剩余18小时32分")
    private String countdown;

    /** 是否本次新建 (true=新创建, false=已存在的记录) */
    @Schema(description = "是否本次新建：true=新创建，false=已有记录", example = "true")
    private Boolean isNew;
}
