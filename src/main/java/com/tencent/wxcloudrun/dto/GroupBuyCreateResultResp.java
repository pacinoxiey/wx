package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class GroupBuyCreateResultResp {

    /** QR task ID returned by /api/group-buy/create. */
    private Long id;

    /** PROCESSING, SUCCESS, or FAILED. */
    private String status;

    /** Suggested client behavior: KEEP_TOAST, SUCCESS_TOAST, SAME_PRODUCT_DIALOG, or FAIL_DIALOG. */
    private String action;

    /** User-facing text for toast or dialog. */
    private String message;

    /** Submitted QR link. */
    private String link;

    /** True when success matched an existing active product instead of creating a new group. */
    private Boolean sameProduct;

    /** New or matched group-buy record, present when status is SUCCESS. */
    private GroupBuyResp groupBuy;

    /** Backward-compatible failure flag for older clients. */
    private Boolean failed;
}
