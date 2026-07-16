package com.tencent.wxcloudrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "拼团创建结果确认请求")
public class GroupBuyCreateConfirmReq {

    /** JOIN_EXISTING = directly participate, CREATE_NEW = directly start a new group. */
    @Schema(description = "确认动作：JOIN_EXISTING=加入已有同款拼团，CREATE_NEW=继续创建新拼团", example = "JOIN_EXISTING")
    private Action action;

    @Schema(description = "创建结果确认动作")
    public enum Action {
        JOIN_EXISTING,
        CREATE_NEW
    }
}
