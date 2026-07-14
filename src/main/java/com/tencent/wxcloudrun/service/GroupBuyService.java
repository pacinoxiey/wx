package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.GroupBuyResp;
import com.tencent.wxcloudrun.dto.GroupBuySearchReq;
import com.tencent.wxcloudrun.dto.SearchHomeResp;

import java.util.List;

public interface GroupBuyService {

    /**
     * 发起拼团 - 解析文本并创建拼团信息
     */
    GroupBuyResp create(String rawText, Boolean force, String openid);

    /**
     * 查看拼团详情
     */
    GroupBuyResp getDetail(Long id);

    com.tencent.wxcloudrun.model.WechatQrTask getQrTask(Long taskId);

    /**
     * 我发起的拼团列表
     */
    List<GroupBuyResp> getMyInitiated(String openid, Integer status, String keyword, Integer page, Integer pageSize);

    /**
     * 拼团广场 - 搜索进行中的拼团 (expire_time > NOW())
     */
    List<GroupBuyResp> searchActive(GroupBuySearchReq req);

    /**
     * 拼团广场 - 总数
     */
    int countActive(GroupBuySearchReq req);

    /**
     * 拼团搜索首页 - 推荐品牌、类目 + 最新拼团列表
     */
    SearchHomeResp getHomePage();
}
