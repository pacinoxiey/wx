package com.tencent.wxcloudrun.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;

@Data
@Schema(description = "统一 API 响应")
public final class ApiResponse {

  @Schema(description = "业务状态码：0=成功，-1=失败", example = "0")
  private Integer code;
  @Schema(description = "错误信息，成功时为空字符串", example = "")
  private String errorMsg;
  @Schema(description = "响应数据，具体结构由接口决定")
  private Object data;

  private ApiResponse(int code, String errorMsg, Object data) {
    this.code = code;
    this.errorMsg = errorMsg;
    this.data = data;
  }
  
  public static ApiResponse ok() {
    return new ApiResponse(0, "", new HashMap<>());
  }

  public static ApiResponse ok(Object data) {
    return new ApiResponse(0, "", data);
  }

  public static ApiResponse error(String errorMsg) {
    return new ApiResponse(-1, errorMsg, new HashMap<>());
  }

  public static ApiResponse error(String errorMsg, Object data) {
    return new ApiResponse(-1, errorMsg, data);
  }
}
