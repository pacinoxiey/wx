package com.tencent.wxcloudrun.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "计数器更新请求")
public class CounterRequest {

  // `action`：`string` 类型，枚举值
  // 等于 `"inc"` 时，表示计数加一
  // 等于 `"clear"` 时，表示计数重置（清零）
  @Schema(description = "操作类型：inc=计数加一，clear=清零", example = "inc", allowableValues = {"inc", "clear"})
  private String action;

}
