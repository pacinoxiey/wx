package com.tencent.wxcloudrun.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 拼团搜索首页配置 —— 推荐品牌和类目关键字本地配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "groupbuy.home")
public class SearchHomeConfig {

    /** 推荐品牌 */
    private List<String> brands = new ArrayList<>();

    /** 推荐类目 */
    private List<String> categories = new ArrayList<>();
}
