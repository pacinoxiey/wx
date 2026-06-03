package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.UserKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserKeywordMapper {

    int insert(UserKeyword keyword);

    int deleteById(@Param("id") Long id, @Param("openid") String openid);

    List<UserKeyword> selectByOpenid(@Param("openid") String openid);

    /** 查关键词是否已存在 */
    UserKeyword selectByOpenidAndKeyword(@Param("openid") String openid,
                                          @Param("keyword") String keyword);
}
