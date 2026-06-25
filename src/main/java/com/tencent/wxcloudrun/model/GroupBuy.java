package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuy implements Serializable {

    private Long id;

    /** 用户粘贴的原始文本 */
    private String rawText;

    /** 来源平台 (拼多多/京东等) */
    private String platform;

    /** 商品名称 (从文本解析) */
    private String productName;

    /** 商品描述 (【...】及后面的描述文字) */
    private String productDesc;

    /** 拼团价格 */
    private BigDecimal groupPrice;

    /** 剩余名额 */
    private Integer remainingSlots;

    /** 分享口令码 */
    private String shareCode;

    /** 商品图片路径 */
    private String imageUrl;

    /** 原始分享链接 */
    private String shareUrl;

    /** 发起人用户 openid */
    private String initiatorId;

    /** 过期时间 (创建时间+24h) */
    private LocalDateTime expireTime;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
