package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GroupBuyResp {

    private Long id;
    private String link;
    private String inputType;

    /** 来源平台 */
    private String platform;

    /** 商品名称 */
    private String productName;

    /** 拼团价格 */
    private BigDecimal groupPrice;

    /** 剩余名额 */
    private Integer remainingSlots;

    /** 商品图片路径 */
    private String imageUrl;

    /** 原始链接 */
    private String shareUrl;

    /** 状态: 1=进行中 2=已过期 */
    private Integer status;

    /** 过期时间 (Unix时间戳，秒) */
    private Long expireTime;

    /** 倒计时描述 (如"剩余18小时32分") */
    private String countdown;

    /** 是否本次新建 (true=新创建, false=已存在的记录) */
    private Boolean isNew;
}
