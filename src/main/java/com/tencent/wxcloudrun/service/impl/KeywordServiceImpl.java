package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.UserKeywordMapper;
import com.tencent.wxcloudrun.model.UserKeyword;
import com.tencent.wxcloudrun.service.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KeywordServiceImpl implements KeywordService {

    @Autowired
    private UserKeywordMapper userKeywordMapper;

    @Override
    public UserKeyword addKeyword(String openid, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("关键词不能为空");
        }
        String kw = keyword.trim();
        // 查重
        UserKeyword existing = userKeywordMapper.selectByOpenidAndKeyword(openid, kw);
        if (existing != null) {
            throw new IllegalArgumentException("该关键词已关注");
        }
        UserKeyword uk = new UserKeyword();
        uk.setOpenid(openid);
        uk.setKeyword(kw);
        userKeywordMapper.insert(uk);
        log.info("添加关注关键词: openid={}, keyword={}, id={}", openid, kw, uk.getId());
        return uk;
    }

    @Override
    public void deleteKeyword(Long id, String openid) {
        int rows = userKeywordMapper.deleteById(id, openid);
        if (rows == 0) {
            throw new IllegalArgumentException("关键词不存在或无权删除");
        }
        log.info("删除关注关键词: id={}, openid={}", id, openid);
    }

    @Override
    public List<UserKeyword> listKeywords(String openid) {
        return userKeywordMapper.selectByOpenid(openid);
    }
}
