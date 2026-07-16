package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.WxUserContext;
import com.tencent.wxcloudrun.dto.GroupBuyCreateConfirmReq;
import com.tencent.wxcloudrun.dto.GroupBuyCreateReq;
import com.tencent.wxcloudrun.dto.GroupBuyCreateResultResp;
import com.tencent.wxcloudrun.dto.GroupBuyResp;
import com.tencent.wxcloudrun.dto.GroupBuySearchReq;
import com.tencent.wxcloudrun.dto.SearchHomeResp;
import com.tencent.wxcloudrun.model.UserKeyword;
import com.tencent.wxcloudrun.service.GroupBuyService;
import com.tencent.wxcloudrun.service.KeywordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "拼团接口", description = "拼团首页、创建、查询、搜索和提醒相关接口")
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
    @Operation(summary = "获取拼团首页", description = "返回推荐品牌、推荐类目以及首页所需的拼团基础数据。")
    public ApiResponse home() {
        SearchHomeResp resp = groupBuyService.getHomePage();
        log.info("GET /api/group-buy/home 响应: {}", resp);
        return ApiResponse.ok(resp);
    }

    /**
     * 发起拼团 - 粘贴文字链接，解析并创建
     */
    @PostMapping("/create")
    @Operation(summary = "发起拼团", description = "根据用户粘贴的拼团链接或口令解析并创建拼团。链接类型只允许 HTTP/HTTPS URL，口令类型不允许直接传 URL。")
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
    @Operation(summary = "查询创建结果", description = "根据创建任务 ID 轮询拼团解析/创建结果。")
    public ApiResponse getCreateResult(@Parameter(description = "创建任务 ID", required = true)
                                       @PathVariable Long id) {
        try {
            GroupBuyCreateResultResp resp = groupBuyService.getCreateResult(id);
            return ApiResponse.ok(resp);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }


    @PostMapping("/create-result/{id}/confirm")
    @Operation(summary = "确认创建结果", description = "当解析到相同商品时，由客户端确认加入已有拼团或继续创建新拼团。")
    public ApiResponse confirmCreateResult(@Parameter(description = "创建任务 ID", required = true)
                                           @PathVariable Long id,
                                           @RequestBody GroupBuyCreateConfirmReq req) {
        try {
            if (req == null || req.getAction() == null) {
                return ApiResponse.error("action is required");
            }
            GroupBuyCreateResultResp resp = groupBuyService.confirmCreateResult(id, req.getAction(), currentUser());
            return ApiResponse.ok(resp);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查看拼团详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查看拼团详情", description = "根据拼团 ID 查询商品、价格、剩余名额、过期时间等详情。")
    public ApiResponse getDetail(@Parameter(description = "拼团 ID", required = true)
                                 @PathVariable Long id) {
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
    @Operation(summary = "查询我发起的拼团", description = "按状态、关键词和分页条件查询当前微信用户发起过的拼团列表。")
    public ApiResponse getMyInitiated(@Parameter(description = "拼团状态：1=进行中，2=已过期")
                                      @RequestParam(required = false) Integer status,
                                      @Parameter(description = "商品名称或关键词")
                                      @RequestParam(required = false) String keyword,
                                      @Parameter(description = "页码，从 1 开始")
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @Parameter(description = "每页条数")
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
    @Operation(summary = "搜索拼团广场", description = "按搜索词或预置标签查询进行中的拼团。keyword 为空时返回全部进行中的拼团。")
    public ApiResponse search(@Parameter(description = "搜索关键词，作为一个整体进行模糊匹配")
                              @RequestParam(required = false) String keyword,
                              @Parameter(description = "标签表达式，多个标签使用 & 分隔")
                              @RequestParam(required = false) String tags,
                              @Parameter(description = "页码，从 1 开始")
                              @RequestParam(defaultValue = "1") Integer page,
                              @Parameter(description = "每页条数")
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
    @Operation(summary = "查询好物提醒", description = "根据当前用户关注的关键词自动匹配命中的拼团列表。")
    public ApiResponse reminder(@Parameter(description = "是否隐藏已过期拼团")
                                @RequestParam(defaultValue = "true") Boolean hideExpired,
                                @Parameter(description = "页码，从 1 开始")
                                @RequestParam(defaultValue = "1") Integer page,
                                @Parameter(description = "每页条数")
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
