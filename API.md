# 拼团广场 API 文档

## 通用格式

所有接口返回格式：

```json
{
  "code": 0,
  "errorMsg": "",
  "data": {}
}
```

| 字段 | 说明 |
|------|------|
| code | 0=成功，非0=失败 |
| errorMsg | 错误信息 |
| data | 响应数据 |

---

## 1. 搜索首页

```
GET /api/group-buy/home
```

**参数：** 无

**响应 data：**

```json
{
  "brands": ["鲜明", "蓝氏", "网易严选", "领先"],
  "categories": ["猫砂", "猫粮", "狗粮", "零食", "玩具", "日用品"]
}
```

> 推荐品牌和类目在 `application.yml` 中的 `groupbuy.home` 下配置，修改后重启即可生效。

---

## 2. 搜索/列表

```
GET /api/group-buy/search
```

**参数：**

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| keyword | 否 | - | 手写搜索词，整体模糊匹配 share_url |
| tags | 否 | - | 多选预制标签，用 `&` 分隔，每个标签独立 OR 匹配 |
| hideExpired | 否 | true | 是否隐藏已过期，false=显示全部 |
| page | 否 | 1 | 页码 |
| pageSize | 否 | 20 | 每页条数 |

> **重要：** `keyword` 中的 `&` 不做拆分（作为整体关键词）；`tags` 用 `&` 拆分后每个词独立匹配。

**调用示例：**

```
# 全部列表
GET /api/group-buy/search

# 手写关键词
GET /api/group-buy/search?keyword=皇家

# 手写关键词含 &（整体匹配）
GET /api/group-buy/search?keyword=李宁%26安踏

# 多选预制标签（各自匹配）
GET /api/group-buy/search?tags=鲜明%26蓝氏

# 手写 + 标签组合
GET /api/group-buy/search?keyword=猫砂&tags=鲜明%26蓝氏&page=1&pageSize=10
```

**响应 data：**

```json
{
  "list": [
    {
      "id": 1,
      "platform": "拼多多",
      "productName": "【喵梵思】",
      "productDesc": "【喵梵思】猫砂除臭结团",
      "groupPrice": 28.80,
      "remainingSlots": 2,
      "shareCode": "ABC001",
      "shareUrl": "https://mobile.yangkeduo.com/goods.html?id=12345",
      "initiatorId": "user001",
      "status": 1,
      "expireTime": 1751385600,
      "countdown": "剩余19小时30分",
      "createdAt": 1751299200,
      "isNew": null
    }
  ],
  "total": 5,
  "page": 1,
  "pageSize": 10
}
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 拼团ID |
| link | string | 用于重新打开的原始输入；二维码为 URL，口令为原始口令文本 |
| inputType | string | 输入类型：`QR_CODE` 或 `TOKEN` |
| platform | string | 来源平台（拼多多/京东/淘宝等） |
| productName | string | 商品名称（【...】部分） |
| productDesc | string | 商品描述（【...】及其后面的描述文字） |
| groupPrice | decimal | 拼团价格 |
| remainingSlots | int | 剩余名额 |
| shareCode | string | 口令码 |
| shareUrl | string | 原始链接 |
| initiatorId | string | 发起人 openid |
| status | int | 1=进行中，2=已过期，根据 expireTime 动态判定 |
| expireTime | long | 过期时间，Unix 时间戳（秒） |
| createdAt | long | 创建时间，Unix 时间戳（秒） |
| countdown | string | 倒计时描述，如"剩余8小时30分" |
| isNew | boolean | 仅 create 接口返回，true=新创建，false=已存在 |

---

## 3. 发起拼团

```
POST /api/group-buy/create
Content-Type: application/json
```

**请求体：**

```json
{
  "type": "LINK",
  "rawText": "https://mobile.yangkeduo.com/...",
  "force": false
}
```

| 字段 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| type | 是 | - | `LINK`=二维码链接，`TOKEN`=拼多多口令文本 |
| rawText | 是 | - | 与 `type` 对应的二维码 URL 或口令文本 |
| force | 否 | false | TOKEN 商品描述重复时是否强制新建；相同链接或口令始终视为重复 |

`LINK` 必须是 HTTP/HTTPS URL；`TOKEN` 不能是 URL。

### 3.1 二维码链接创建

二维码链接只创建异步解析任务，不会创建占位 `group_buy` 数据。创建成功仅返回任务 ID；`pdd_helper` 解析成功后负责创建实际拼团数据：

```json
{
  "code": 0,
  "errorMsg": "",
  "data": { "id": 123 }
}
```

### 3.2 口令创建

`type: "TOKEN"` 时同步解析口令。成功时 `data` 为单条拼团记录，格式同搜索结果；其中 `isNew` 为 `true`。

### 3.3 重复创建

同一二维码 URL 或口令码已存在时，创建接口返回失败；`data` 为已有拼团记录，包含可继续使用的 `id` 与 `link`。`link` 可能为口令文本或二维码 URL。

```json
{
  "code": -1,
  "errorMsg": "duplicate group buy",
  "data": {
    "id": 123,
    "link": "https://mobile.yangkeduo.com/..."
  }
}
```

---

## 4. 创建结果轮询

```
GET /api/group-buy/create-result/{id}
```

仅用于轮询二维码链接创建任务是否失败，不返回商品详情。

处理中或成功：

```json
{
  "code": 0,
  "errorMsg": "",
  "data": { "id": 123, "failed": false }
}
```

最终失败时，`link` 为提交失败的二维码链接：

```json
{
  "code": 0,
  "errorMsg": "",
  "data": {
    "id": 123,
    "failed": true,
    "link": "https://mobile.yangkeduo.com/..."
  }
}
```

---

## 5. 拼团详情

```
GET /api/group-buy/{id}
```

**示例：**

```
GET /api/group-buy/1
```

**响应 data：** 同搜索结果单条记录格式。

---

## 6. 我发起的拼团

```
GET /api/group-buy/my-initiated
```

**参数：**

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| status | 否 | - | 1=进行中，2=已过期，不传=全部 |
| keyword | 否 | - | 模糊匹配 share_url |
| page | 否 | 1 | 页码 |
| pageSize | 否 | 10 | 每页条数 |

**示例：**

```
# 全部
GET /api/group-buy/my-initiated

# 只看进行中
GET /api/group-buy/my-initiated?status=1

# 模糊搜索
GET /api/group-buy/my-initiated?status=2&keyword=jd&page=1&pageSize=10
```

**响应 data：** 数组，每条格式同搜索结果。

---

## 7. 好物提醒

```
GET /api/group-buy/reminder
```

**说明：** 自动获取当前用户所有关注关键词，用 OR 逻辑匹配进行中的拼团。

**参数：**

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| hideExpired | 否 | true | 是否隐藏已过期，false=显示全部 |
| page | 否 | 1 | 页码 |
| pageSize | 否 | 20 | 每页条数 |

**示例：**

```
GET /api/group-buy/reminder
GET /api/group-buy/reminder?hideExpired=false
GET /api/group-buy/reminder?page=1&pageSize=10
```

**响应 data：** 同搜索结果格式。

---

## 8. 我的关注 - 关键词管理

### 7.1 添加关键词

```
POST /api/keyword/add?keyword=xxx
```

| 参数 | 必填 | 说明 |
|------|------|------|
| keyword | 是 | 关键词，重复添加会报错 |

**响应 data：**

```json
{ "id": 1, "keyword": "yangkeduo", "createdAt": 1751299200 }
```

### 7.2 删除关键词

```
DELETE /api/keyword/{id}
```

### 7.3 我的关注列表

```
GET /api/keyword/list
```

**响应 data：**

```json
[
  { "id": 1, "keyword": "yangkeduo", "createdAt": 1751299200 },
  { "id": 2, "keyword": "jd", "createdAt": 1751299200 }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 关键词ID |
| keyword | string | 关键词 |
| createdAt | long | 创建时间，Unix 时间戳（秒） |

---

## 错误码

| code | 说明 |
|------|------|
| 0 | 成功 |
| -1 | 失败，详见 errorMsg |
