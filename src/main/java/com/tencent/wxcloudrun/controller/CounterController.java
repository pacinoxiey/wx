package com.tencent.wxcloudrun.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.CounterRequest;
import com.tencent.wxcloudrun.model.Counter;
import com.tencent.wxcloudrun.service.CounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * counter控制器
 */
@RestController
@Tag(name = "计数器接口", description = "示例计数器的查询和更新接口")
public class CounterController {

  final CounterService counterService;
  final Logger logger;

  public CounterController(@Autowired CounterService counterService) {
    this.counterService = counterService;
    this.logger = LoggerFactory.getLogger(CounterController.class);
  }


  /**
   * 获取当前计数
   * @return API response json
   */
  @GetMapping(value = "/api/count")
  @Operation(summary = "获取当前计数", description = "查询示例计数器当前的计数值。")
  ApiResponse get() {
    logger.info("GET /api/count 请求");
    Optional<Counter> counter = counterService.getCounter(1);
    Integer count = 0;
    if (counter.isPresent()) {
      count = counter.get().getCount();
    }
    logger.info("GET /api/count 响应: count={}", count);
    return ApiResponse.ok(count);
  }


  /**
   * 更新计数，自增或者清零
   * @param request {@link CounterRequest}
   * @return API response json
   */
  @PostMapping(value = "/api/count")
  @Operation(summary = "更新计数", description = "根据 action 对计数器执行加一或清零操作。action=inc 表示加一，action=clear 表示清零。")
  ApiResponse create(@RequestBody CounterRequest request) {
    logger.info("POST /api/count 请求: action={}", request.getAction());

    Optional<Counter> curCounter = counterService.getCounter(1);
    if (request.getAction().equals("inc")) {
      Integer count = 1;
      if (curCounter.isPresent()) {
        count += curCounter.get().getCount();
      }
      Counter counter = new Counter();
      counter.setId(1);
      counter.setCount(count);
      counterService.upsertCount(counter);
      logger.info("POST /api/count 响应: inc → count={}", count);
      return ApiResponse.ok(count);
    } else if (request.getAction().equals("clear")) {
      if (!curCounter.isPresent()) {
        logger.info("POST /api/count 响应: clear → count=0 (不存在)");
        return ApiResponse.ok(0);
      }
      counterService.clearCount(1);
      logger.info("POST /api/count 响应: clear → count=0");
      return ApiResponse.ok(0);
    } else {
      logger.warn("POST /api/count 参数异常: action={}", request.getAction());
      return ApiResponse.error("参数action错误");
    }
  }
  
}
