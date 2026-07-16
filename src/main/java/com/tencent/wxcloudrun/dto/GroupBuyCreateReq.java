package com.tencent.wxcloudrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发起拼团请求")
public class GroupBuyCreateReq {

    @Schema(description = "用户输入类型")
    public enum Type {
        LINK,
        TOKEN
    }

    /** A PDD link or token, selected by type. */
    @Schema(description = "输入类型：LINK=链接，TOKEN=口令", example = "LINK")
    private Type type;

    /** 用户粘贴的拼团原始文本 */
    @Schema(description = "用户粘贴的原始拼团链接或口令文本", example = "https://example.com/group-buy/xxx")
    private String rawText;

    /** 是否强制新建 (已存在相同拼团时，true=强制新建，false=返回已有) */
    @Schema(description = "是否强制新建。存在相同拼团时，true=强制新建，false=返回已有记录", example = "false")
    private Boolean force;
}
