package com.tencent.wxcloudrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "拼团创建结果响应")
public class GroupBuyCreateResultResp {

    /** QR task ID returned by /api/group-buy/create. */
    @Schema(description = "创建任务 ID，用于客户端轮询结果", example = "1001")
    private Long id;

    /** PROCESSING, SUCCESS, or FAILED. */
    @Schema(description = "任务状态：PROCESSING=处理中，SUCCESS=成功，FAILED=失败", example = "SUCCESS")
    private String status;

    /** Suggested client behavior: KEEP_TOAST, SUCCESS_TOAST, SAME_PRODUCT_DIALOG, or FAIL_DIALOG. */
    @Schema(description = "建议客户端动作：KEEP_TOAST=继续提示处理中，SUCCESS_TOAST=成功提示，SAME_PRODUCT_DIALOG=同款确认弹窗，FAIL_DIALOG=失败弹窗", example = "SUCCESS_TOAST")
    private String action;

    /** User-facing text for toast or dialog. */
    @Schema(description = "展示给用户的提示文案", example = "拼团创建成功")
    private String message;

    /** Submitted QR link. */
    @Schema(description = "用户提交的二维码链接或分享链接")
    private String link;

    /** True when success matched an existing active product instead of creating a new group. */
    @Schema(description = "是否命中已有同款进行中的拼团", example = "false")
    private Boolean sameProduct;

    /** New or matched group-buy record, present when status is SUCCESS. */
    @Schema(description = "创建成功或命中同款时返回的拼团信息")
    private GroupBuyResp groupBuy;

    /** Backward-compatible failure flag for older clients. */
    @Schema(description = "兼容旧客户端的失败标记", example = "false")
    private Boolean failed;
}
