package com.tencent.wxcloudrun.config;

/**
 * 微信用户上下文
 * 微信云托管模式下，网关自动注入 x-wx-openid 请求头
 */
public class WxUserContext {

    private static final ThreadLocal<String> OPENID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> UNIONID_HOLDER = new ThreadLocal<>();

    public static void setOpenid(String openid) {
        OPENID_HOLDER.set(openid);
    }

    public static String getOpenid() {
        return OPENID_HOLDER.get();
    }

    public static void setUnionid(String unionid) {
        UNIONID_HOLDER.set(unionid);
    }

    public static String getUnionid() {
        return UNIONID_HOLDER.get();
    }

    public static void clear() {
        OPENID_HOLDER.remove();
        UNIONID_HOLDER.remove();
    }
}
