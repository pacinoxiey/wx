package com.tencent.wxcloudrun.service;

import java.math.BigDecimal;

/**
 * 拼多多拼团文本解析服务
 *
 * 解析示例:
 * 输入: "D:/⇥WYIZCIUyt4HIP⇤ 复制并打开拼多多APP，28.8元拼团购买【喵梵思】王贵皮黄皮木薯猫砂瞬吸结团防臭...，仅剩2个名额"
 * 输出: {平台=拼多多, 价格=28.8, 商品名=【喵梵思】王贵皮黄皮木薯猫砂..., 剩余名额=2, 口令=WYIZCIUyt4HIP}
 */
public class TextParserService {

    /**
     * 解析结果
     */
    public static class ParseResult {
        private String platform;
        private String productName;
        private String productDesc;
        private BigDecimal groupPrice;
        private Integer remainingSlots;
        private String shareCode;
        private String shareUrl;

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getProductDesc() { return productDesc; }
        public void setProductDesc(String productDesc) { this.productDesc = productDesc; }

        public BigDecimal getGroupPrice() { return groupPrice; }
        public void setGroupPrice(BigDecimal groupPrice) { this.groupPrice = groupPrice; }

        public Integer getRemainingSlots() { return remainingSlots; }
        public void setRemainingSlots(Integer remainingSlots) { this.remainingSlots = remainingSlots; }

        public String getShareCode() { return shareCode; }
        public void setShareCode(String shareCode) { this.shareCode = shareCode; }

        public String getShareUrl() { return shareUrl; }
        public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }
    }

    /**
     * 解析拼多多拼团文本
     */
    public ParseResult parse(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("拼团文本不能为空");
        }

        ParseResult result = new ParseResult();

        // 1. 提取平台
        result.setPlatform(extractPlatform(rawText));

        // 2. 提取口令码 (⇥...⇤ 中的内容)
        result.setShareCode(extractShareCode(rawText));

        // 3. 提取价格 (xx.x元 或 xx元)
        result.setGroupPrice(extractPrice(rawText));

        // 4. 提取商品名
        result.setProductName(extractProductName(rawText));

        // 4.1 提取商品描述（【...】及后面的描述文字）
        result.setProductDesc(extractProductDesc(rawText));

        // 5. 提取剩余名额
        result.setRemainingSlots(extractRemainingSlots(rawText));

        // 6. 提取原始链接(如有)
        result.setShareUrl(extractShareUrl(rawText));

        // 校验必要字段
        if (result.getProductName() == null || result.getProductName().isEmpty()) {
            throw new IllegalArgumentException("无法从文本中解析出商品名称，请检查文本格式");
        }

        // 设置默认值
        if (result.getPlatform() == null) result.setPlatform("拼多多");
        if (result.getRemainingSlots() == null) result.setRemainingSlots(1);

        return result;
    }

    private String extractPlatform(String text) {
        if (text.contains("拼多多")) return "拼多多";
        if (text.contains("京东")) return "京东";
        if (text.contains("淘宝")) return "淘宝";
        if (text.contains("美团")) return "美团";
        if (text.contains("抖音")) return "抖音";
        return "拼多多";
    }

    private String extractShareCode(String text) {
        // 匹配 ⇥...⇤ (拼多多常见格式)
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("⇥([^⇤]+)⇤").matcher(text);
        if (m.find()) return m.group(1);

        // 匹配 ￥...￥ (淘宝常见格式)
        m = java.util.regex.Pattern.compile("￥([^￥]+)￥").matcher(text);
        if (m.find()) return m.group(1);

        // 匹配长串大写字母数字组合
        m = java.util.regex.Pattern.compile("([A-Z0-9]{8,})").matcher(text);
        if (m.find()) return m.group(1);

        return null;
    }

    private BigDecimal extractPrice(String text) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)\\s*元").matcher(text);
        if (m.find()) {
            return new BigDecimal(m.group(1));
        }
        return null;
    }

    private String extractProductName(String text) {
        // 优先匹配 【...】 中的内容
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("【(.+?)】").matcher(text);
        if (m.find()) {
            String name = m.group(1);
            // 继续向后匹配，可能有多段【】
            StringBuilder sb = new StringBuilder("【").append(name).append("】");
            while (m.find()) {
                sb.append("【").append(m.group(1)).append("】");
            }
            return sb.toString();
        }

        // 匹配 "购买xxx，" 或 "购买xxx，" 中的商品名
        m = java.util.regex.Pattern.compile("购买(.+?)[，,]").matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }

        // 最后尝试匹配 "拼团" 后面的内容直到末尾或标点
        m = java.util.regex.Pattern.compile("拼团(.+?)$").matcher(text);
        if (m.find()) {
            String s = m.group(1).trim();
            // 去掉开头的标点或"购买"
            s = s.replaceAll("^购买\\s*", "");
            if (s.length() > 2) return s;
        }

        return null;
    }

    /**
     * 提取商品描述 —— 从【...】开始直到逗号/名额标记
     * 示例: "购买【喵梵思】王贵皮黄皮木薯猫砂瞬吸结团防臭...，仅剩2个名额"
     * 返回: "【喵梵思】王贵皮黄皮木薯猫砂瞬吸结团防臭"
     */
    private String extractProductDesc(String text) {
        int start = text.indexOf("【");
        if (start >= 0) {
            int end = text.length();
            for (String marker : new String[]{"，", "，", "仅剩", "还差"}) {
                int pos = text.indexOf(marker, start);
                if (pos > 0 && pos < end) end = pos;
            }
            String desc = text.substring(start, end).trim();
            desc = desc.replaceAll("[.…]+$", "").trim();
            if (desc.length() > 2) return desc;
        }

        // 兜底: 返回 productName
        return extractProductName(text);
    }

    private Integer extractRemainingSlots(String text) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("仅剩\\s*(\\d+)\\s*个?名?额?").matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        m = java.util.regex.Pattern.compile("还差\\s*(\\d+)\\s*人").matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    private String extractShareUrl(String text) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(https?://[^\\s，,]+)").matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
