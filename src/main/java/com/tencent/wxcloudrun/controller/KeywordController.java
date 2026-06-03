package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.WxUserContext;
import com.tencent.wxcloudrun.dto.UserKeywordResp;
import com.tencent.wxcloudrun.model.UserKeyword;
import com.tencent.wxcloudrun.service.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/keyword")
public class KeywordController {

    @Autowired
    private KeywordService keywordService;

    private String currentUser() {
        String openid = WxUserContext.getOpenid();
        if (openid == null || openid.isEmpty()) {
            openid = "unknown_user";
        }
        return openid;
    }

    /**
     * 添加关注关键词
     */
    @PostMapping("/add")
    public ApiResponse add(@RequestParam String keyword) {
        try {
            UserKeyword uk = keywordService.addKeyword(currentUser(), keyword);
            log.info("POST /api/keyword/add 响应: id={}, keyword={}", uk.getId(), uk.getKeyword());
            return ApiResponse.ok(uk);
        } catch (IllegalArgumentException e) {
            log.error("POST /api/keyword/add 异常: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除关注关键词
     */
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        try {
            keywordService.deleteKeyword(id, currentUser());
            log.info("DELETE /api/keyword/{} 响应: 已删除", id);
            return ApiResponse.ok();
        } catch (IllegalArgumentException e) {
            log.error("DELETE /api/keyword/{} 异常: {}", id, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 我的关注关键词列表
     */
    @GetMapping("/list")
    public ApiResponse list() {
        List<UserKeyword> list = keywordService.listKeywords(currentUser());
        List<UserKeywordResp> result = list.stream().map(UserKeywordResp::from).collect(Collectors.toList());
        log.info("GET /api/keyword/list 响应: 共{}条", result.size());
        return ApiResponse.ok(result);
    }
}
