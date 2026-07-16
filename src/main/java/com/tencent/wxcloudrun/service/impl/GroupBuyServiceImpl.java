package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.config.SearchHomeConfig;
import com.tencent.wxcloudrun.dao.GroupBuyMapper;
import com.tencent.wxcloudrun.dto.GroupBuyCreateConfirmReq;
import com.tencent.wxcloudrun.dto.GroupBuyCreateResultResp;
import com.tencent.wxcloudrun.dto.GroupBuyResp;
import com.tencent.wxcloudrun.dto.GroupBuySearchReq;
import com.tencent.wxcloudrun.dto.SearchHomeResp;
import com.tencent.wxcloudrun.model.GroupBuy;
import com.tencent.wxcloudrun.model.WechatQrTask;
import com.tencent.wxcloudrun.service.GroupBuyService;
import com.tencent.wxcloudrun.service.TextParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GroupBuyServiceImpl implements GroupBuyService {

    @Autowired
    private GroupBuyMapper groupBuyMapper;

    @Autowired
    private SearchHomeConfig searchHomeConfig;

    private final TextParserService textParserService = new TextParserService();

    @Override
    @Transactional
    public GroupBuyResp create(String rawText, Boolean force, String openid) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("拼团文本不能为空");
        }

        // 解析文本
        if (isHttpUrl(rawText)) {
            return createFromLink(rawText.trim(), openid);
        }

        TextParserService.ParseResult parsed = textParserService.parse(rawText);
        // 链接校验: share_url 或 share_code 必须至少有一个有效
        String shareUrl = parsed.getShareUrl();
        String shareCode = parsed.getShareCode();
        if ((shareUrl == null || shareUrl.isEmpty()) && (shareCode == null || shareCode.isEmpty())) {
            throw new IllegalArgumentException("未识别到有效的拼团链接或口令码，请检查文本格式");
        }
        // URL 域名白名单校验
        // if (shareUrl != null && !shareUrl.isEmpty() && !isValidPlatformUrl(shareUrl)) {
        //     throw new IllegalArgumentException("暂不支持该平台的拼团链接，仅支持拼多多/京东/淘宝/美团/抖音");
        // }

        // force=true 时跳过去重，允许相同口令或商品重复创建。
        GroupBuy existing = groupBuyMapper.selectActiveByShareCodeOrUrl(
                parsed.getShareCode(), parsed.getShareUrl());
        if (existing != null && !Boolean.TRUE.equals(force)) {
            GroupBuyResp resp = toResp(existing);
            resp.setIsNew(false);
            return resp;
        }

        GroupBuy groupBuy = groupBuyMapper.selectActiveByProductDesc(parsed.getProductDesc());
        if (groupBuy != null && !Boolean.TRUE.equals(force)) {
            log.info("已存在相同的商品, all={}, 返回已有记录", rawText);
            GroupBuyResp resp = toResp(groupBuy);
            resp.setIsNew(false);
            return resp;
        }


        // 构建实体
        GroupBuy gb = new GroupBuy();
        gb.setInputType("TOKEN");
        gb.setRawText(rawText);
        gb.setPlatform(parsed.getPlatform());
        gb.setProductName(parsed.getProductName());
        gb.setProductDesc(parsed.getProductDesc());
        gb.setGroupPrice(parsed.getGroupPrice());
        gb.setRemainingSlots(parsed.getRemainingSlots());
        gb.setShareCode(shareCode);
        gb.setShareUrl(shareUrl);
        gb.setInitiatorId(openid);

        LocalDateTime now = LocalDateTime.now();
        gb.setCreatedAt(now);
        gb.setExpireTime(now.plusHours(24)); // 24小时有效期

        groupBuyMapper.insert(gb);

        GroupBuyResp resp = toResp(gb);
        resp.setIsNew(true);
        return resp;
    }

    private GroupBuyResp createFromLink(String link, String openid) {
        if (!isValidPlatformUrl(link)) {
            throw new IllegalArgumentException("unsupported group-buy link");
        }
        GroupBuy existingGroupBuy = groupBuyMapper.selectActiveByShareCodeOrUrl(null, link);
        if (existingGroupBuy != null) {
            GroupBuyResp resp = toResp(existingGroupBuy);
            resp.setIsNew(false);
            return resp;
        }
        WechatQrTask existingTask = groupBuyMapper.selectWechatQrTaskByUrl(link);
        if (existingTask != null) {
            GroupBuyResp resp = new GroupBuyResp();
            resp.setId(existingTask.getId());
            resp.setLink(existingTask.getQrUrl());
            resp.setIsNew(false);
            return resp;
        }

        WechatQrTask task = new WechatQrTask();
        task.setQrUrl(link);
        task.setInitiatorId(openid);
        groupBuyMapper.insertWechatQrTask(task);
        GroupBuyResp resp = new GroupBuyResp();
        resp.setId(task.getId());
        resp.setLink(link);
        resp.setIsNew(true);
        return resp;
    }

    private boolean isHttpUrl(String value) {
        return value != null && value.trim().matches("(?i)^https?://\\S+$");
    }

    @Override
    public GroupBuyResp getDetail(Long id) {
        GroupBuy gb = groupBuyMapper.selectById(id);
        if (gb == null) {
            throw new IllegalArgumentException("拼团信息不存在");
        }
        return toResp(gb);
    }

    @Override
    public WechatQrTask getQrTask(Long taskId) {
        return groupBuyMapper.selectWechatQrTaskById(taskId);
    }

    @Override
    public GroupBuyCreateResultResp getCreateResult(Long taskId) {
        WechatQrTask task = groupBuyMapper.selectWechatQrTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("create task does not exist");
        }

        GroupBuyCreateResultResp resp = new GroupBuyCreateResultResp();
        resp.setId(task.getId());
        resp.setLink(task.getQrUrl());
        resp.setSameProduct(Boolean.TRUE.equals(task.getSameProduct()));

        String qrStatus = task.getQrStatus();
        if ("FAILED".equals(qrStatus)) {
            resp.setStatus("FAILED");
            resp.setAction("FAIL_DIALOG");
            resp.setMessage(task.getQrError() != null && !task.getQrError().trim().isEmpty()
                    ? task.getQrError()
                    : "没有识别到有效的二维码，请重新传一次吧");
            resp.setFailed(true);
            return resp;
        }

        if ("SUCCESS".equals(qrStatus)) {
            GroupBuy groupBuy = Boolean.TRUE.equals(task.getSameProduct())
                    ? findActiveSameProductFromTask(task)
                    : groupBuyMapper.selectByQrTaskId(taskId);
            if (groupBuy == null && task.getGroupBuyId() != null) {
                groupBuy = groupBuyMapper.selectById(task.getGroupBuyId());
            }
            if (groupBuy == null && task.getMatchedGroupBuyId() != null) {
                groupBuy = groupBuyMapper.selectById(task.getMatchedGroupBuyId());
            }

            resp.setStatus("SUCCESS");
            resp.setGroupBuy(groupBuy != null ? toResp(groupBuy) : null);
            resp.setAction(Boolean.TRUE.equals(task.getSameProduct())
                    ? "SAME_PRODUCT_DIALOG"
                    : "SUCCESS_TOAST");
            resp.setMessage(Boolean.TRUE.equals(task.getSameProduct())
                    ? "检测到有相同的产品，是否直接参与拼团"
                    : "小主，已成功发布咯");
            resp.setFailed(false);
            return resp;
        }

        resp.setStatus("PROCESSING");
        resp.setAction("KEEP_TOAST");
        resp.setMessage("正在获取商品信息，请小主耐心等待");
        resp.setFailed(false);
        return resp;
    }

    @Override
    @Transactional
    public GroupBuyCreateResultResp confirmCreateResult(Long taskId,
                                                        GroupBuyCreateConfirmReq.Action action,
                                                        String openid) {
        if (action == null) {
            throw new IllegalArgumentException("action is required");
        }

        WechatQrTask task = groupBuyMapper.selectWechatQrTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("create task does not exist");
        }
        if (!"SUCCESS".equals(task.getQrStatus())) {
            throw new IllegalArgumentException("create task is not ready");
        }
        if (!Boolean.TRUE.equals(task.getSameProduct())) {
            return getCreateResult(taskId);
        }

        if (action == GroupBuyCreateConfirmReq.Action.JOIN_EXISTING) {
            GroupBuy matched = findActiveSameProductFromTask(task);
            if (matched == null) {
                throw new IllegalArgumentException("matched group buy does not exist");
            }
            return buildConfirmedResult(task, matched, "JOIN_EXISTING", "已为你找到相同产品拼团");
        }

        GroupBuy created = groupBuyMapper.selectByQrTaskId(taskId);
        if (created == null) {
            created = createQrGroupBuyFromTask(task, openid);
            groupBuyMapper.bindWechatQrTaskGroupBuy(taskId, created.getId());
        }
        return buildConfirmedResult(task, created, "SUCCESS_TOAST", "小主，已成功发布咯");
    }

    private GroupBuyCreateResultResp buildConfirmedResult(WechatQrTask task,
                                                          GroupBuy groupBuy,
                                                          String action,
                                                          String message) {
        GroupBuyCreateResultResp resp = new GroupBuyCreateResultResp();
        resp.setId(task.getId());
        resp.setStatus("SUCCESS");
        resp.setAction(action);
        resp.setMessage(message);
        resp.setLink(task.getQrUrl());
        resp.setSameProduct(action.equals("JOIN_EXISTING"));
        resp.setGroupBuy(toResp(groupBuy));
        resp.setFailed(false);
        return resp;
    }

    private GroupBuy findActiveSameProductFromTask(WechatQrTask task) {
        String productDesc = extractTaskProductDesc(task);
        if (productDesc != null && !productDesc.isEmpty()) {
            GroupBuy sameProduct = groupBuyMapper.selectActiveByProductDesc(productDesc);
            if (sameProduct != null) {
                return sameProduct;
            }
        }

        GroupBuy matched = groupBuyMapper.selectMatchedByQrTaskId(task.getId());
        if (matched == null && task.getMatchedGroupBuyId() != null) {
            matched = groupBuyMapper.selectActiveById(task.getMatchedGroupBuyId());
        }
        return matched;
    }

    private String extractTaskProductDesc(WechatQrTask task) {
        if (task.getQrRawText() != null && !task.getQrRawText().trim().isEmpty()) {
            try {
                return parseQrRawText(task.getQrRawText()).getProductDesc();
            } catch (IllegalArgumentException e) {
                log.warn("Failed to parse qr_raw_text for same-product lookup, taskId={}", task.getId());
            }
        }
        GroupBuy matched = groupBuyMapper.selectMatchedByQrTaskId(task.getId());
        return matched != null ? matched.getProductDesc() : null;
    }

    private GroupBuy createQrGroupBuyFromTask(WechatQrTask task, String openid) {
        if (task.getQrRawText() == null || task.getQrRawText().trim().isEmpty()) {
            throw new IllegalArgumentException("QR task has no parsed product text");
        }

        TextParserService.ParseResult parsed = parseQrRawText(task.getQrRawText());
        GroupBuy gb = new GroupBuy();
        gb.setInputType("QR_CODE");
        gb.setRawText(task.getQrUrl());
        gb.setPlatform(parsed.getPlatform());
        gb.setProductName(parsed.getProductName());
        gb.setProductDesc(parsed.getProductDesc());
        gb.setGroupPrice(parsed.getGroupPrice());
        gb.setRemainingSlots(parsed.getRemainingSlots());
        gb.setShareCode(parsed.getShareCode());
        gb.setImageUrl(null);
        gb.setShareUrl(task.getQrUrl());
        gb.setInitiatorId(openid != null && !openid.isEmpty()
                ? openid
                : task.getInitiatorId());

        LocalDateTime now = LocalDateTime.now();
        gb.setCreatedAt(now);
        gb.setExpireTime(now.plusHours(24));
        groupBuyMapper.insert(gb);
        return gb;
    }

    private TextParserService.ParseResult parseQrRawText(String rawText) {
        TextParserService.ParseResult parsed = new TextParserService.ParseResult();
        String normalized = rawText.trim();
        String[] lines = normalized.split("\\r?\\n");

        String productDesc = firstNonBlank(lines);
        parsed.setPlatform("拼多多");
        parsed.setProductName(productDesc);
        parsed.setProductDesc(productDesc);
        parsed.setGroupPrice(extractQrPrice(normalized));
        parsed.setRemainingSlots(extractQrRemainingSlots(normalized));
        parsed.setShareUrl(null);
        parsed.setShareCode(null);

        if (parsed.getProductName() == null || parsed.getProductName().isEmpty()) {
            throw new IllegalArgumentException("无法从二维码解析结果中读取商品名称");
        }
        if (parsed.getRemainingSlots() == null) {
            parsed.setRemainingSlots(1);
        }
        return parsed;
    }

    private String firstNonBlank(String[] lines) {
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
        }
        return null;
    }

    private java.math.BigDecimal extractQrPrice(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(text);
        while (matcher.find()) {
            String value = matcher.group(1);
            int end = matcher.end();
            String suffix = text.substring(end, Math.min(text.length(), end + 4));
            if (suffix.contains("元") || suffix.contains("\\n") || suffix.contains("\r")) {
                return new java.math.BigDecimal(value);
            }
        }
        return null;
    }

    private Integer extractQrRemainingSlots(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("还差\\s*(\\d+)\\s*人").matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        matcher = java.util.regex.Pattern.compile("仅剩\\s*(\\d+)\\s*个?名额").matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    @Override
    public List<GroupBuyResp> getMyInitiated(String openid, Integer status, String keyword, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<GroupBuy> list = groupBuyMapper.selectByInitiator(openid, status, keyword, offset, pageSize);
        List<GroupBuyResp> result = new ArrayList<>();
        for (GroupBuy gb : list) {
            result.add(toResp(gb));
        }
        return result;
    }

    /**
     * 将 tags 按 & 拆分 + keyword 作为整体合并为一个关键词列表
     */
    private List<String> mergeKeywords(GroupBuySearchReq req) {
        List<String> result = new ArrayList<>();

        // 用户手写的关键词 → 作为一个整体
        if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            result.add(req.getKeyword().trim());
        }

        // 预制标签 → 用 & 分隔，每个独立
        if (req.getTags() != null && !req.getTags().trim().isEmpty()) {
            for (String tag : req.getTags().split("&")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }

        return result;
    }

    @Override
    public List<GroupBuyResp> searchActive(GroupBuySearchReq req) {
        int offset = (req.getPage() - 1) * req.getPageSize();
        List<String> keywords = mergeKeywords(req);
        List<GroupBuy> list = groupBuyMapper.selectActive(keywords, req.getHideExpired(), offset, req.getPageSize());
        List<GroupBuyResp> result = new ArrayList<>();
        for (GroupBuy gb : list) {
            result.add(toResp(gb));
        }
        return result;
    }

    @Override
    public int countActive(GroupBuySearchReq req) {
        List<String> keywords = mergeKeywords(req);
        return groupBuyMapper.countActive(keywords, req.getHideExpired());
    }

    @Override
    public SearchHomeResp getHomePage() {
        SearchHomeResp resp = new SearchHomeResp();

        // 推荐品牌和类目（从本地配置读取）
        resp.setBrands(searchHomeConfig.getBrands());
        resp.setCategories(searchHomeConfig.getCategories());

        // 最新拼团列表
        // GroupBuySearchReq req = new GroupBuySearchReq();
        // req.setPage(page != null ? page : 1);
        // req.setPageSize(pageSize != null ? pageSize : 20);
        // List<GroupBuyResp> list = searchActive(req);
        // int total = countActive(req);

        // resp.setList(list);
        // resp.setTotal(total);
        // resp.setPage(req.getPage());
        // resp.setPageSize(req.getPageSize());

        return resp;
    }

    /**
     * 实体转响应DTO，状态根据expire_time动态判定
     */
    private GroupBuyResp toResp(GroupBuy gb) {
        GroupBuyResp resp = new GroupBuyResp();
        resp.setId(gb.getId());
        resp.setInputType(gb.getInputType());
        resp.setLink(gb.getShareUrl() != null && !gb.getShareUrl().isEmpty()
                ? gb.getShareUrl() : gb.getRawText());
        resp.setPlatform(gb.getPlatform());
        resp.setProductName(gb.getProductName());
        resp.setGroupPrice(gb.getGroupPrice());
        resp.setRemainingSlots(gb.getRemainingSlots());
        resp.setImageUrl(gb.getImageUrl());
        resp.setShareUrl(gb.getShareUrl());
        resp.setExpireTime(toEpochSecond(gb.getExpireTime()));

        // 动态判定状态和倒计时
        LocalDateTime now = LocalDateTime.now();
        if (gb.getExpireTime() != null && gb.getExpireTime().isAfter(now)) {
            resp.setStatus(1); // 进行中
            Duration d = Duration.between(now, gb.getExpireTime());
            long hours = d.toHours();
            long minutes = d.toMinutes() % 60;
            if (hours > 0) {
                resp.setCountdown("剩余" + hours + "小时" + minutes + "分");
            } else {
                resp.setCountdown("剩余" + minutes + "分");
            }
        } else {
            resp.setStatus(2); // 已过期
            resp.setCountdown("已过期");
        }

        return resp;
    }

    /**
     * LocalDateTime 转 Unix时间戳(秒)，使用 Asia/Shanghai 时区
     */
    private Long toEpochSecond(LocalDateTime dt) {
        if (dt == null) {
            return null;
        }
        return dt.atZone(ZoneId.of("Asia/Shanghai")).toEpochSecond();
    }

    /**
     * 校验 share_url 是否属于支持的平台域名
     */
    private boolean isValidPlatformUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        String lower = url.toLowerCase();
        return lower.contains("yangkeduo.com")
            || lower.contains("pinduoduo.com")
            || lower.contains("jd.com")
            || lower.contains("taobao.com")
            || lower.contains("tmall.com")
            || lower.contains("tb.cn")
            || lower.contains("meituan.com")
            || lower.contains("douyin.com")
            || lower.contains("jinritemai.com");
    }
}
