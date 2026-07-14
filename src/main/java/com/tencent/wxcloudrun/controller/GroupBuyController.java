package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.WxUserContext;
import com.tencent.wxcloudrun.dto.GroupBuyCreateReq;
import com.tencent.wxcloudrun.dto.GroupBuyResp;
import com.tencent.wxcloudrun.dto.GroupBuySearchReq;
import com.tencent.wxcloudrun.dto.SearchHomeResp;
import com.tencent.wxcloudrun.model.UserKeyword;
import com.tencent.wxcloudrun.model.WechatQrTask;
import com.tencent.wxcloudrun.service.GroupBuyService;
import com.tencent.wxcloudrun.service.KeywordService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/group-buy")
public class GroupBuyController {

    @Autowired
    private GroupBuyService groupBuyService;

    @Autowired
    private KeywordService keywordService;

    /**
     * 获取当前微信用户标识
     */
    private String currentUser() {
        String openid = WxUserContext.getOpenid();
        if (openid == null || openid.isEmpty()) {
            log.error("无法获取微信用户信息");
            openid = "unknown_user";
            // throw new IllegalArgumentException("无法获取微信用户信息");
        }
        return openid;
    }

    /**
     * 拼团搜索首页 - 推荐品牌、类目 + 最新拼团列表
     */
    @GetMapping("/home")
    public ApiResponse home() {
        SearchHomeResp resp = groupBuyService.getHomePage();
        log.info("GET /api/group-buy/home 响应: {}", resp);
        return ApiResponse.ok(resp);
    }

    /**
     * 发起拼团 - 粘贴文字链接，解析并创建
     */
    @PostMapping("/create")
    public ApiResponse create(@RequestBody GroupBuyCreateReq req) {
        try {
            if (req.getRawText() == null || req.getRawText().trim().isEmpty()) {
                return ApiResponse.error("rawText is required");
            }
            if (req.getType() == null) {
                return ApiResponse.error("type is required");
            }
            boolean isHttpUrl = req.getRawText().trim().matches("(?i)^https?://\\S+$");
            if (req.getType() == GroupBuyCreateReq.Type.LINK && !isHttpUrl) {
                return ApiResponse.error("LINK type requires an http URL");
            }
            if (req.getType() == GroupBuyCreateReq.Type.TOKEN && isHttpUrl) {
                return ApiResponse.error("TOKEN type cannot use an http URL");
            }
            if (req.getType() == GroupBuyCreateReq.Type.LINK) {
                GroupBuyResp resp = groupBuyService.create(req.getRawText(), req.getForce(), currentUser());
                if (!Boolean.TRUE.equals(resp.getIsNew())) {
                    return ApiResponse.error("duplicate group buy", resp);
                }
                Map<String, Object> data = new HashMap<>();
                data.put("id", resp.getId());
                return ApiResponse.ok(data);
            }
            log.info("POST /api/group-buy/create 请求: type={}, rawText长度={}, force={}",
                    req.getType(),
                    req.getRawText() != null ? req.getRawText().length() : 0, req.getForce());
            GroupBuyResp resp = groupBuyService.create(req.getRawText(), req.getForce(), currentUser());
            log.info("POST /api/group-buy/create 响应: id={}, isNew={}, productName={}",
                    resp.getId(), resp.getIsNew(), resp.getProductName());
            return Boolean.TRUE.equals(resp.getIsNew())
                    ? ApiResponse.ok(resp) : ApiResponse.error("duplicate group buy", resp);
        } catch (IllegalArgumentException e) {
            log.error("POST /api/group-buy/create 异常: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/create-result/{id}")
    public ApiResponse getCreateResult(@PathVariable Long id) {
        try {
            WechatQrTask task = groupBuyService.getQrTask(id);
            if (task == null) {
                return ApiResponse.error("create task does not exist");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", task.getId());
            boolean failed = "FAILED".equals(task.getQrStatus());
            data.put("failed", failed);
            if (failed) {
                data.put("link", task.getQrUrl());
            }
            return ApiResponse.ok(data);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查看拼团详情
     */
    @GetMapping("/{id}")
    public ApiResponse getDetail(@PathVariable Long id) {
        try {
            log.info("GET /api/group-buy/{} 请求", id);
            GroupBuyResp resp = groupBuyService.getDetail(id);
            log.info("GET /api/group-buy/{} 响应: productName={}, status={}", id, resp.getProductName(), resp.getStatus());
            return ApiResponse.ok(resp);
        } catch (IllegalArgumentException e) {
            log.error("GET /api/group-buy/{} 异常: {}", id, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 我发起的拼团列表
     */
    @GetMapping("/my-initiated")
    public ApiResponse getMyInitiated(@RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("GET /api/group-buy/my-initiated 请求: status={}, keyword={}, page={}, pageSize={}",
                status, keyword, page, pageSize);
        List<GroupBuyResp> list = groupBuyService.getMyInitiated(currentUser(), status, keyword, page, pageSize);
        log.info("GET /api/group-buy/my-initiated 响应: 共{}条", list.size());
        return ApiResponse.ok(list);
    }

    /**
     * 拼团广场 - 搜索/列表（keyword 为空时返回全部进行中的拼团）
     */
    @GetMapping("/search")
    public ApiResponse search(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String tags,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("GET /api/group-buy/search 请求: keyword={}, tags={}, page={}, pageSize={}", keyword, tags, page, pageSize);
        GroupBuySearchReq req = new GroupBuySearchReq();
        req.setKeyword(keyword);
        req.setTags(tags);
        req.setPage(page);
        req.setPageSize(pageSize);
        List<GroupBuyResp> list = groupBuyService.searchActive(req);
        int total = groupBuyService.countActive(req);
        log.info("GET /api/group-buy/search 响应: 共{}条, total={}", list.size(), total);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        return ApiResponse.ok(data);
    }

    /**
     * 好物提醒 - 自动匹配所有关注关键词，返回命中的进行中拼团
     */
    @GetMapping("/reminder")
    public ApiResponse reminder(@RequestParam(defaultValue = "true") Boolean hideExpired,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "20") Integer pageSize) {
        String openid = currentUser();
        log.info("GET /api/group-buy/reminder 请求: hideExpired={}, page={}, pageSize={}", hideExpired, page, pageSize);

        // 获取用户所有关注关键词
        List<UserKeyword> keywords = keywordService.listKeywords(openid);
        if (keywords.isEmpty()) {
            log.info("GET /api/group-buy/reminder 响应: 无关注关键词，返回空");
            Map<String, Object> data = new HashMap<>();
            data.put("list", java.util.Collections.emptyList());
            data.put("total", 0);
            data.put("page", page);
            data.put("pageSize", pageSize);
            return ApiResponse.ok(data);
        }

        // 用所有关键词 OR 匹配
        String tags = keywords.stream().map(UserKeyword::getKeyword).collect(Collectors.joining("&"));
        GroupBuySearchReq req = new GroupBuySearchReq();
        req.setTags(tags);
        req.setHideExpired(hideExpired);
        req.setPage(page);
        req.setPageSize(pageSize);
        List<GroupBuyResp> list = groupBuyService.searchActive(req);
        int total = groupBuyService.countActive(req);
        log.info("GET /api/group-buy/reminder 响应: 关键词{}个，命中{}条, total={}", keywords.size(), list.size(), total);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        return ApiResponse.ok(data);
    }
}
