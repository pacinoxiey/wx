package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.GroupBuy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupBuyMapper {

    int insert(GroupBuy groupBuy);

    GroupBuy selectById(@Param("id") Long id);

    List<GroupBuy> selectByInitiator(@Param("initiatorId") String initiatorId,
                                      @Param("status") Integer status,
                                      @Param("keyword") String keyword,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    List<GroupBuy> selectActive(@Param("keywords") List<String> keywords,
                                @Param("hideExpired") Boolean hideExpired,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    int countActive(@Param("keywords") List<String> keywords,
                    @Param("hideExpired") Boolean hideExpired);

    /**
     * 根据 share_code 或 share_url 查找进行中的拼团（去重用）
     */
    GroupBuy selectActiveByShareCodeOrUrl(@Param("shareCode") String shareCode,
                                          @Param("shareUrl") String shareUrl);


    GroupBuy selectActiveByProductDesc(@Param("productDesc") String productDesc);
}
