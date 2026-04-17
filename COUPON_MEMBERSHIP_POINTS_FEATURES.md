# 优惠券、会员卡和积分系统功能说明

## 一、已完成的 backend 功能

### 1. 数据库表设计
- ✅ coupon_template - 优惠券模板表
- ✅ user_coupon - 用户优惠券表
- ✅ membership_level - 会员等级表
- ✅ user_membership - 用户会员表
- ✅ points_log - 积分流水表
- ✅ points_product - 积分商品表
- ✅ points_order - 积分订单表
- ✅ 修改 orders 表添加 member_discount 和 user_coupon_id 字段

### 2. Entity 实体类
- ✅ CouponTemplate
- ✅ UserCoupon
- ✅ MembershipLevel
- ✅ UserMembership
- ✅ PointsLog
- ✅ PointsProduct
- ✅ PointsOrder

### 3. Mapper 接口
- ✅ CouponTemplateMapper
- ✅ UserCouponMapper
- ✅ MembershipLevelMapper
- ✅ UserMembershipMapper
- ✅ PointsLogMapper
- ✅ PointsProductMapper
- ✅ PointsOrderMapper

### 4. Service 层实现
- ✅ CouponServiceImpl
  - getCouponTemplates() - 获取优惠券列表
  - receiveCoupon() - 领取优惠券
  - getMyCoupons() - 我的优惠券
  - getAvailableCoupons() - 获取可用优惠券
  - useCoupon() - 使用优惠券
  - calculateBestCoupon() - 计算最优优惠券
  - calculateDiscount() - 计算折扣金额

- ✅ MembershipServiceImpl
  - getMembershipLevels() - 获取会员等级列表
  - getMyMembership() - 获取我的会员信息
  - upgradeMembership() - 升级会员
  - addGrowthValue() - 添加成长值
  - calculatePoints() - 计算应得积分
  - addPoints() - 添加积分
  - consumePoints() - 消费积分

- ✅ PointsServiceImpl
  - getPointsProducts() - 获取积分商品列表
  - exchangePointsProduct() - 兑换积分商品
  - getMyPointsOrders() - 我的积分订单

### 5. Controller 层
- ✅ CouponController
  - GET /api/coupon/template/list - 获取优惠券列表
  - POST /api/coupon/receive/{templateId} - 领取优惠券
  - GET /api/coupon/my/list?status=0 - 我的优惠券
  - GET /api/coupon/my/available?orderAmount=100 - 获取可用优惠券

- ✅ MembershipController
  - GET /api/membership/levels - 获取会员等级列表
  - GET /api/membership/my - 我的会员信息
  - POST /api/membership/growth/add?value=100 - 添加成长值

- ✅ PointsController
  - GET /api/points/products - 获取积分商品列表
  - POST /api/points/exchange/{productId} - 兑换积分商品
  - GET /api/points/my/orders - 我的积分订单

### 6. 订单系统集成
- ✅ OrderService 集成优惠券使用
- ✅ OrderService 集成会员折扣
- ✅ OrderService 集成积分赠送（支付成功后）

## 二、已完成的前端功能

### 1. 页面创建
- ✅ CouponCenter.vue - 优惠券中心
- ✅ MembershipCenter.vue - 会员中心
- ✅ PointsMall.vue - 积分商城

### 2. 路由配置
- ✅ /coupon - 优惠券中心
- ✅ /membership - 会员中心
- ✅ /points - 积分商城

### 3. 导航菜单
- ✅ 用户下拉菜单添加会员中心入口
- ✅ 用户下拉菜单添加优惠券入口
- ✅ 用户下拉菜单添加积分商城入口

## 三、核心业务逻辑

### 1. 优惠券系统
- 支持满减券和折扣券两种类型
- 每人限领限制
- 最低消费金额限制
- 有效期管理
- 自动计算最优优惠券

### 2. 会员卡系统
- 4 个等级：普通会员、白银会员、黄金会员、钻石会员
- 成长值决定等级
- 不同等级享受不同折扣
- 不同等级享受不同积分倍率
- 支持自动升级

### 3. 积分系统
- 购物赠送积分（根据会员等级倍率）
- 积分兑换商品
- 积分扣减
- 积分流水记录

### 4. 订单集成
- 下单时自动计算会员折扣
- 下单时可使用优惠券
- 支付成功后自动赠送积分
- 订单中记录使用的优惠券和会员折扣

## 四、测试数据

### 会员等级初始化数据
```sql
- 普通会员：0 成长值，1.0 折扣，1.0 积分倍率
- 白银会员：1000 成长值，0.98 折扣，1.2 积分倍率
- 黄金会员：5000 成长值，0.95 折扣，1.5 积分倍率
- 钻石会员：20000 成长值，0.90 折扣，2.0 积分倍率
```

### 优惠券初始化数据
```sql
- 新人专享券：满 100 减 20
- 满减优惠券：满 300 减 50
- 折扣券：满 200 打 8 折（最高优惠 100 元）
```

### 积分商品初始化数据
```sql
- 宠物玩具：100 积分 + 10 元
- 宠物零食：200 积分 + 20 元
- 宠物沐浴露：300 积分 + 30 元
- 宠物窝垫：500 积分 + 50 元
```

## 五、使用说明

### 1. 访问优惠券中心
- 登录系统
- 点击用户头像下拉菜单
- 选择"优惠券"进入优惠券中心
- 可领取优惠券或查看已领取的优惠券

### 2. 访问会员中心
- 登录系统
- 点击用户头像下拉菜单
- 选择"会员中心"查看会员信息
- 可查看当前等级、积分、成长值等
- 满足条件时可升级会员等级

### 3. 访问积分商城
- 登录系统
- 点击用户头像下拉菜单
- 选择"积分商城"
- 可使用积分兑换商品
- 查看兑换记录

### 4. 购物使用优惠券
- 将商品加入购物车
- 进入购物车页面
- 点击"结算"按钮
- 在订单确认页选择优惠券
- 提交订单后自动使用优惠券并享受会员折扣

## 六、API 测试示例

### 1. 获取优惠券列表
```bash
GET http://localhost:8082/api/coupon/template/list
```

### 2. 领取优惠券
```bash
POST http://localhost:8082/api/coupon/receive/1
Authorization: Bearer {token}
```

### 3. 获取我的优惠券
```bash
GET http://localhost:8082/api/coupon/my/list?status=0
Authorization: Bearer {token}
```

### 4. 获取会员等级列表
```bash
GET http://localhost:8082/api/membership/levels
```

### 5. 获取我的会员信息
```bash
GET http://localhost:8082/api/membership/my
Authorization: Bearer {token}
```

### 6. 获取积分商品列表
```bash
GET http://localhost:8082/api/points/products
```

### 7. 兑换积分商品
```bash
POST http://localhost:8082/api/points/exchange/1
Authorization: Bearer {token}
```

## 七、注意事项

1. 所有操作需要先登录（JWT 认证）
2. 优惠券有数量限制和每人限领限制
3. 会员等级根据成长值自动判定
4. 积分在支付成功后自动赠送
5. 优惠券在订单提交时使用，订单取消后自动退回
6. H2 数据库为内存模式，重启后数据会重置

## 八、后续优化建议

1. 增加优惠券活动管理后台
2. 增加会员任务系统（签到、评价等获取成长值）
3. 增加积分过期机制
4. 增加组合优惠券（多张券叠加使用）
5. 增加会员专属商品
6. 增加积分抽奖功能
