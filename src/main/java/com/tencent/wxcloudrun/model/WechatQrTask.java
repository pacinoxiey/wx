package com.tencent.wxcloudrun.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Asynchronous QR-link parsing task. A groupBuyId is assigned by pdd_helper
 * only after the link has been parsed and a group_buy record is created.
 */
@Data
@Schema(description = "微信二维码链接解析任务")
public class WechatQrTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique task ID returned to the client for polling. */
    @Schema(description = "任务 ID", example = "1001")
    private Long id;

    /** Parsed group_buy ID; null while the task has not completed. */
    @Schema(description = "解析成功后创建或关联的拼团 ID")
    private Long groupBuyId;

    /** Submitted QR link. */
    @Schema(description = "用户提交的二维码链接")
    private String qrUrl;

    /** OpenID of the user who submitted the link. */
    @Schema(description = "提交任务的微信用户 openid")
    private String initiatorId;

    /** PENDING, PROCESSING, RETRYING, SUCCESS, or FAILED. */
    @Schema(description = "二维码解析状态：PENDING=待处理，PROCESSING=处理中，RETRYING=重试中，SUCCESS=成功，FAILED=失败", example = "PROCESSING")
    private String qrStatus;

    /** Scheduler retry count. */
    @Schema(description = "调度重试次数", example = "0")
    private Integer qrAttempts;

    /** Last processing error, when available. */
    @Schema(description = "最近一次处理错误信息")
    private String qrError;

    /** Time of the last processing attempt. */
    @Schema(description = "二维码任务最近处理时间")
    private LocalDateTime qrProcessedAt;

    /** Earliest time at which the task may be retried. */
    @Schema(description = "二维码任务下次尝试时间")
    private LocalDateTime qrNextAttemptAt;

    /** Raw text extracted by pdd_helper from the QR target page. */
    @Schema(description = "二维码目标页解析出的原始文本")
    private String qrRawText;

    /** Whether pdd_helper found an active group with the same product. */
    @Schema(description = "是否找到同款进行中的拼团", example = "false")
    private Boolean sameProduct;

    /** Existing group_buy ID to join when sameProduct is true. */
    @Schema(description = "命中的同款拼团 ID")
    private Long matchedGroupBuyId;

    /** Task creation time. */
    @Schema(description = "任务创建时间")
    private LocalDateTime createdAt;

    /** Task last update time. */
    @Schema(description = "任务更新时间")
    private LocalDateTime updatedAt;
}
