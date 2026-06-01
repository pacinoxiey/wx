package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.config.SearchHomeConfig;
import com.tencent.wxcloudrun.dao.GroupBuyMapper;
import com.tencent.wxcloudrun.dto.GroupBuyResp;
import com.tencent.wxcloudrun.dto.GroupBuySearchReq;
import com.tencent.wxcloudrun.dto.SearchHomeResp;
import com.tencent.wxcloudrun.model.GroupBuy;
import com.tencent.wxcloudrun.service.GroupBuyService;
import com.tencent.wxcloudrun.service.TextParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public GroupBuyResp create(String rawText, Boolean force, String openid) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("拼团文本不能为空");
        }

        // 解析文本
        TextParserService.ParseResult parsed = textParserService.parse(rawText);

        // 去重: 根据 share_code/share_url 查找是否已有进行中的相同拼团
        GroupBuy existing = groupBuyMapper.selectActiveByShareCodeOrUrl(
                parsed.getShareCode(), parsed.getShareUrl());
        if (existing != null && !Boolean.TRUE.equals(force)) {
            log.info("已存在相同的拼团, id={}, shareCode={}, shareUrl={}, 返回已有记录",
                    existing.getId(), parsed.getShareCode(), parsed.getShareUrl());
            GroupBuyResp resp = toResp(existing);
            resp.setIsNew(false);
            return resp;
        }

        // 构建实体
        GroupBuy gb = new GroupBuy();
        gb.setRawText(rawText);
        gb.setPlatform(parsed.getPlatform());
        gb.setProductName(parsed.getProductName());
        gb.setGroupPrice(parsed.getGroupPrice());
        gb.setRemainingSlots(parsed.getRemainingSlots());
        gb.setShareCode(parsed.getShareCode());
        gb.setShareUrl(parsed.getShareUrl());
        gb.setInitiatorId(openid);

        LocalDateTime now = LocalDateTime.now();
        gb.setCreatedAt(now);
        gb.setExpireTime(now.plusHours(24)); // 24小时有效期

        groupBuyMapper.insert(gb);

        GroupBuyResp resp = toResp(gb);
        resp.setIsNew(true);
        return resp;
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
        List<GroupBuy> list = groupBuyMapper.selectActive(keywords, offset, req.getPageSize());
        List<GroupBuyResp> result = new ArrayList<>();
        for (GroupBuy gb : list) {
            result.add(toResp(gb));
        }
        return result;
    }

    @Override
    public int countActive(GroupBuySearchReq req) {
        List<String> keywords = mergeKeywords(req);
        return groupBuyMapper.countActive(keywords);
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
        resp.setPlatform(gb.getPlatform());
        resp.setProductName(gb.getProductName());
        resp.setGroupPrice(gb.getGroupPrice());
        resp.setRemainingSlots(gb.getRemainingSlots());
        resp.setShareCode(gb.getShareCode());
        resp.setShareUrl(gb.getShareUrl());
        resp.setInitiatorId(gb.getInitiatorId());
        resp.setExpireTime(toEpochSecond(gb.getExpireTime()));
        resp.setCreatedAt(toEpochSecond(gb.getCreatedAt()));

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
}
