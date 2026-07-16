package com.tencent.wxcloudrun.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "拼团数据库记录")
public class GroupBuy implements Serializable {

    @Schema(description = "拼团 ID", example = "1001")
    private Long id;
    @Schema(description = "输入来源类型：LINK=链接，TOKEN=口令")
    private String inputType;

    /** 用户粘贴的原始文本 */
    @Schema(description = "用户粘贴的原始文本")
    private String rawText;

    /** 来源平台 (拼多多/京东等) */
    @Schema(description = "来源平台，例如：拼多多、京东", example = "拼多多")
    private String platform;

    /** 商品名称 (从文本解析) */
    @Schema(description = "商品名称", example = "鲜肉猫粮 2kg")
    private String productName;

    /** 商品描述 (【...】及后面的描述文字) */
    @Schema(description = "商品描述")
    private String productDesc;

    /** 拼团价格 */
    @Schema(description = "拼团价格", example = "29.90")
    private BigDecimal groupPrice;

    /** 剩余名额 */
    @Schema(description = "剩余参团名额", example = "2")
    private Integer remainingSlots;

    /** 分享口令码 */
    @Schema(description = "分享口令码")
    private String shareCode;

    /** 商品图片路径 */
    @Schema(description = "商品图片地址")
    private String imageUrl;

    /** 原始分享链接 */
    @Schema(description = "原始分享链接")
    private String shareUrl;

    /** 发起人用户 openid */
    @Schema(description = "发起人微信 openid")
    private String initiatorId;

    /** 过期时间 (创建时间+24h) */
    @Schema(description = "拼团过期时间")
    private LocalDateTime expireTime;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
