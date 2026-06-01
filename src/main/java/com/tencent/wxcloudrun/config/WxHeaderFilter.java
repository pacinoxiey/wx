package com.tencent.wxcloudrun.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 微信云托管请求头过滤器
 * 云托管网关会自动注入 x-wx-openid / x-wx-unionid
 * 本 Filter 将其提取到 WxUserContext 中，方便后续使用
 */
@Component
@Order(1)
public class WxHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // 微信云托管注入的请求头
            String openid = httpRequest.getHeader("x-wx-openid");
            String unionid = httpRequest.getHeader("x-wx-unionid");

            // 本地调试兜底: 如果没读到 openid，用固定测试值
            if (openid == null || openid.isEmpty()) {
                openid = httpRequest.getHeader("x-debug-openid");
            }

            WxUserContext.setOpenid(openid);
            WxUserContext.setUnionid(unionid);

            chain.doFilter(request, response);
        } finally {
            WxUserContext.clear();
        }
    }
}
