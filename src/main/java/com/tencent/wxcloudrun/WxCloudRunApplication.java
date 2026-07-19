package com.tencent.wxcloudrun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan(basePackages = {"com.tencent.wxcloudrun.dao"})
public class WxCloudRunApplication {

  public static void main(String[] args) {
    // 设置 JVM 默认时区为北京时间，确保所有时间 API 统一使用 Asia/Shanghai
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    SpringApplication.run(WxCloudRunApplication.class, args);
  }
}
