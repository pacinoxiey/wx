package com.tencent.wxcloudrun.model;

import lombok.Data;

/**
 * Asynchronous QR-link parsing task. A groupBuyId is assigned by pdd_helper
 * only after the link has been parsed and a group_buy record is created.
 */
@Data
public class WechatQrTask {

    /** Unique task ID returned to the client for polling. */
    private Long id;

    /** Parsed group_buy ID; null while the task has not completed. */
    private Long groupBuyId;

    /** Submitted QR link. */
    private String qrUrl;

    /** OpenID of the user who submitted the link. */
    private String initiatorId;

    /** PENDING, PROCESSING, RETRYING, SUCCESS, or FAILED. */
    private String qrStatus;

    /** Scheduler retry count. */
    private Integer qrAttempts;

    /** Last processing error, when available. */
    private String qrError;

    /** Raw text extracted by pdd_helper from the QR target page. */
    private String qrRawText;

    /** Whether pdd_helper found an active group with the same product. */
    private Boolean sameProduct;

    /** Existing group_buy ID to join when sameProduct is true. */
    private Long matchedGroupBuyId;
}
