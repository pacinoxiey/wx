package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.WxUserContext;
import com.tencent.wxcloudrun.dto.UserKeywordResp;
import com.tencent.wxcloudrun.model.UserKeyword;
import com.tencent.wxcloudrun.service.KeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/keyword")
@Tag(name = "关键词接口", description = "用户关注关键词的新增、删除和列表查询")
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
    @Operation(summary = "添加关注关键词", description = "为当前微信用户添加一个关注关键词，用于好物提醒匹配。")
    public ApiResponse add(@Parameter(description = "关注关键词，例如：猫粮", required = true)
                           @RequestParam String keyword) {
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
    @Operation(summary = "删除关注关键词", description = "删除当前微信用户名下指定 ID 的关注关键词。")
    public ApiResponse delete(@Parameter(description = "关键词记录 ID", required = true)
                              @PathVariable Long id) {
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
    @Operation(summary = "查询关注关键词列表", description = "查询当前微信用户已关注的全部关键词。")
    public ApiResponse list() {
        List<UserKeyword> list = keywordService.listKeywords(currentUser());
        List<UserKeywordResp> result = list.stream().map(UserKeywordResp::from).collect(Collectors.toList());
        log.info("GET /api/keyword/list 响应: 共{}条", result.size());
        return ApiResponse.ok(result);
    }
}
