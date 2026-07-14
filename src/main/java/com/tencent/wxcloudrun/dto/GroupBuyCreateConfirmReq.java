package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class GroupBuyCreateConfirmReq {

    /** JOIN_EXISTING = directly participate, CREATE_NEW = directly start a new group. */
    private Action action;

    public enum Action {
        JOIN_EXISTING,
        CREATE_NEW
    }
}
