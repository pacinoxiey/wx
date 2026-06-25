package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/wx")
public class WxCosController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String WX_COS_AUTH_URL = "http://api.weixin.qq.com/_/cos/getauth";

    /**
     * 获取微信 COS 上传授权
     */
    @GetMapping("/cos/auth")
    public ApiResponse getCosAuth() {
        try {
            log.info("GET /api/wx/cos/auth 请求微信COS授权");
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    WX_COS_AUTH_URL, HttpMethod.GET, entity, String.class);
            log.info("GET /api/wx/cos/auth 响应: status={}", response.getStatusCodeValue());
            return ApiResponse.ok(response.getBody());
        } catch (Exception e) {
            log.error("GET /api/wx/cos/auth 异常: {}", e.getMessage());
            return ApiResponse.error("获取COS授权失败: " + e.getMessage());
        }
    }
}
