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
      "productName": "【喵梵思】猫砂除臭结团",
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
| platform | string | 来源平台（拼多多/京东/淘宝等） |
| productName | string | 商品名称 |
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
  "rawText": "D:/⇥WYIZCIUyt4HIP⇤ 复制并打开拼多多APP，28.8元拼团购买【喵梵思】猫砂...，仅剩2个名额",
  "force": false
}
```

| 字段 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| rawText | 是 | - | 用户粘贴的拼团原始文本，后端自动解析 |
| force | 否 | false | 已存在相同拼团时是否强制新建 |

**返回逻辑：**

- `force` 不传或 `false` → 若已有相同 share_code/share_url 的进行中拼团，返回已有记录（`isNew: false`）
- `force: true` → 跳过查重，强制新建（`isNew: true`）
- 无重复 → 新建（`isNew: true`）

**响应 data：** 同搜索结果单条记录格式，多了 `isNew` 字段。

---

## 4. 拼团详情

```
GET /api/group-buy/{id}
```

**示例：**

```
GET /api/group-buy/1
```

**响应 data：** 同搜索结果单条记录格式。

---

## 5. 我发起的拼团

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

## 错误码

| code | 说明 |
|------|------|
| 0 | 成功 |
| 非0 | 失败，详见 errorMsg |
