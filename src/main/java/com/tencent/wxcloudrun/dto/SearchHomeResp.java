package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.util.List;

/**
 * 拼团搜索首页响应
 */
@Data
public class SearchHomeResp {

    /** 推荐品牌 */
    private List<String> brands;

    /** 推荐类目 */
    private List<String> categories;
}
