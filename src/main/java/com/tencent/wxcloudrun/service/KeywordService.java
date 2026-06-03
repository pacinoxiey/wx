package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.UserKeyword;

import java.util.List;

public interface KeywordService {

    /** 添加关注关键词 */
    UserKeyword addKeyword(String openid, String keyword);

    /** 删除关注关键词 */
    void deleteKeyword(Long id, String openid);

    /** 我的关注列表 */
    List<UserKeyword> listKeywords(String openid);
}
