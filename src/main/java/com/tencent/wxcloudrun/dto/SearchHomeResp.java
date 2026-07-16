package com.tencent.wxcloudrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 拼团搜索首页响应
 */
@Data
@Schema(description = "拼团搜索首页响应")
public class SearchHomeResp {

    /** 推荐品牌 */
    @Schema(description = "推荐品牌列表", example = "[\"鲜明\",\"蓝小\"]")
    private List<String> brands;

    /** 推荐类目 */
    @Schema(description = "推荐类目列表", example = "[\"猫粮\",\"狗粮\"]")
    private List<String> categories;
}
