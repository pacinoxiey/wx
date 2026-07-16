package com.tencent.wxcloudrun.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "计数器记录")
public class Counter implements Serializable {

  @Schema(description = "计数器 ID", example = "1")
  private Integer id;

  @Schema(description = "当前计数值", example = "10")
  private Integer count;

  @Schema(description = "创建时间")
  private LocalDateTime createdAt;

  @Schema(description = "更新时间")
  private LocalDateTime updatedAt;
}
