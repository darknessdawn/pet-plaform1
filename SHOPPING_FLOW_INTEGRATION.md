# 购物流程完整集成说明

## 一、完整的购物流程

### 1. 购物车页面 (`/cart`)
- ✅ 展示购物车商品列表
- ✅ 支持商品选择/取消选择
- ✅ 支持数量调整
- ✅ 实时计算选中商品的总价
- ✅ "去结算"按钮跳转到订单确认页

### 2. 订单确认页面 (`/order/confirm`) - 新创建
这是整个购物流程的核心页面，集成了所有优惠功能：

#### 功能模块：

**A. 收货地址管理**
- 显示用户的所有收货地址
- 支持选择默认地址
- 支持添加新地址（弹窗表单）
- 地址信息包含：收货人、电话、省市区、详细地址

**B. 商品信息展示**
- 表格形式展示选中的商品
- 显示：商品图片、名称、单价、数量、小计

**C. 会员折扣自动计算**
- 自动获取用户会员等级
- 根据会员等级自动应用对应折扣
- 显示折扣率（如 9.5 折）
- 实时计算并显示节省金额

**D. 优惠券选择与使用**
- 下拉框选择可用优惠券
- 显示优惠券列表：
  * 满减券：直接显示减免金额
  * 折扣券：显示折扣率和最大优惠
- 自动筛选满足条件的优惠券（如满 300 可用）
- 实时更新优惠金额

**E. 价格计算逻辑**
```
商品总额 = Σ(商品单价 × 数量)
会员折扣后金额 = 商品总额 × 会员折扣率
优惠券折扣 = 根据选择的优惠券类型计算
最终应付金额 = 商品总额 - 会员折扣 - 优惠券折扣
预计获得积分 = 最终应付金额 × 会员积分倍率
```

**F. 订单备注**
- 支持文本输入框填写备注

**G. 提交订单**
- 验证必填项（地址必须选择）
- 调用后端创建订单接口
- 传递参数：
  ```javascript
  {
    addressId: 选择的地址 ID,
    remark: 备注内容,
    couponId: 选择的优惠券 ID（可选）
  }
  ```

### 3. 后端订单处理流程

#### OrderServiceImpl.createOrder() 方法执行步骤：

1. **获取商品信息**
   - 从购物车获取选中的商品
   - 验证商品状态和库存

2. **扣减库存**
   - 使用乐观锁机制扣减库存
   - 防止超卖

3. **计算会员折扣**
   ```java
   UserMembershipVO membership = membershipService.getMyMembership(userId);
   BigDecimal memberDiscount = membership.getDiscountRate();
   BigDecimal discountedAmount = totalAmount.multiply(memberDiscount);
   ```

4. **计算并使用优惠券**
   ```java
   if (orderDTO.getCouponId() != null) {
       couponDiscount = couponService.calculateDiscount(
           orderDTO.getCouponId(), 
           discountedAmount
       );
       // 保存订单后标记优惠券为已使用
       couponService.useCoupon(userId, couponId, orderId);
   }
   ```

5. **计算最终金额**
   ```java
   BigDecimal payAmount = discountedAmount.subtract(couponDiscount);
   order.setPayAmount(payAmount);
   ```

6. **保存订单**
   - 记录订单总金额
   - 记录优惠金额
   - 记录会员折扣率
   - 记录使用的优惠券 ID

7. **清理购物车**
   - 删除已购买的商品项

8. **记录用户行为**
   - 用于推荐算法优化

### 4. 支付成功后赠送积分

#### OrderServiceImpl.payOrder() 方法：
```java
// 支付成功后
int points = membershipService.calculatePoints(
    userId, 
    order.getPayAmount().doubleValue()
);
if (points > 0) {
    membershipService.addPoints(
        userId, 
        points, 
        "ORDER_PAY", 
        orderId, 
        "订单支付赠送积分"
    );
}
```

## 二、数据流转图

```
用户操作
  ↓
购物车选择商品
  ↓
点击"去结算"
  ↓
订单确认页 (/order/confirm)
  ├─ 加载会员信息 → 获取折扣率和积分倍率
  ├─ 加载可用优惠券 → 筛选符合条件的券
  ├─ 选择收货地址
  └─ 选择优惠券
       ↓
  实时计算价格：
  - 商品总额
  - 会员折扣
  - 优惠券折扣
  - 最终应付金额
  - 预计获得积分
       ↓
  点击"提交订单"
       ↓
  后端处理：
  ├─ 验证库存
  ├─ 扣减库存
  ├─ 计算会员折扣
  ├─ 验证并使用优惠券
  ├─ 计算最终金额
  ├─ 保存订单
  └─ 清理购物车
       ↓
  跳转到订单详情页
       ↓
  用户支付
       ↓
  支付成功 → 赠送积分
```

## 三、关键代码示例

### 前端 - 订单确认页价格计算

```javascript
// 商品总额
const totalAmount = computed(() => {
  return cartItems.value.reduce(
    (sum, item) => sum + item.productPrice * item.quantity, 
    0
  ).toFixed(2);
});

// 会员折扣金额
const memberDiscountAmount = computed(() => {
  if (!membership.value.discountRate || membership.value.discountRate >= 1) 
    return 0;
  const discount = parseFloat(totalAmount.value) * 
                   (1 - membership.value.discountRate);
  return discount.toFixed(2);
});

// 优惠券折扣金额
const couponDiscount = computed(() => {
  if (!selectedCouponId.value) return 0;
  const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value);
  
  const amountAfterMemberDiscount = 
      parseFloat(totalAmount.value) - parseFloat(memberDiscountAmount.value);
  
  if (coupon.type === 1) {
    // 满减券
    return coupon.discountAmount;
  } else if (coupon.type === 2) {
    // 折扣券
    let discount = amountAfterMemberDiscount * (1 - coupon.discountRate);
    if (coupon.maxDiscount) {
      discount = Math.min(discount, coupon.maxDiscount);
    }
    return discount.toFixed(2);
  }
  return 0;
});

// 最终应付金额
const payAmount = computed(() => {
  const amount = parseFloat(totalAmount.value) 
                 - parseFloat(memberDiscountAmount.value) 
                 - parseFloat(couponDiscount.value);
  return Math.max(0, amount).toFixed(2);
});

// 预计获得积分
const expectedPoints = computed(() => {
  const pointsRate = membership.value.pointsRate || 1;
  return Math.floor(parseFloat(payAmount.value) * pointsRate);
});
```

### 后端 - 订单创建时的优惠计算

```java
// 计算会员折扣
UserMembershipVO membership = membershipService.getMyMembership(userId);
BigDecimal memberDiscount = BigDecimal.ONE;
if (membership != null && membership.getDiscountRate() != null) {
    memberDiscount = membership.getDiscountRate();
}
BigDecimal discountedAmount = totalAmount.multiply(memberDiscount);

// 使用优惠券
BigDecimal couponDiscount = BigDecimal.ZERO;
Long usedCouponId = null;
if (orderDTO.getCouponId() != null) {
    couponDiscount = couponService.calculateDiscount(
        orderDTO.getCouponId(), 
        discountedAmount
    );
    if (couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
        usedCouponId = orderDTO.getCouponId();
    }
}

// 计算最终金额
BigDecimal payAmount = discountedAmount.subtract(couponDiscount);
if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
    payAmount = BigDecimal.ZERO;
}

// 设置订单属性
order.setTotalAmount(totalAmount);
order.setDiscountAmount(couponDiscount);
order.setPayAmount(payAmount);
order.setMemberDiscount(memberDiscount);
order.setUserCouponId(usedCouponId);
```

## 四、API 接口清单

### 购物车相关
- `GET /api/cart/list` - 获取购物车列表
- `PUT /api/cart/{id}` - 更新购物车商品
- `DELETE /api/cart/{id}` - 删除购物车商品

### 订单相关
- `POST /api/order/create` - 创建订单
- `GET /api/order/{id}` - 查询订单详情
- `POST /api/order/pay` - 支付订单

### 优惠券相关
- `GET /api/coupon/template/list` - 获取优惠券模板
- `POST /api/coupon/receive/{templateId}` - 领取优惠券
- `GET /api/coupon/my/list?status=0` - 我的优惠券
- `GET /api/coupon/my/available?orderAmount=100` - 获取可用优惠券

### 会员相关
- `GET /api/membership/my` - 获取我的会员信息
- `GET /api/membership/levels` - 获取会员等级列表

### 积分相关
- `GET /api/points/products` - 获取积分商品
- `POST /api/points/exchange/{productId}` - 兑换积分商品

### 地址相关
- `GET /api/address/list` - 获取地址列表
- `POST /api/address` - 添加地址

## 五、测试场景

### 场景 1：普通用户无优惠券
- 用户等级：普通会员（无折扣）
- 优惠券：无
- 计算：商品总额 = 应付金额

### 场景 2：黄金会员使用满减券
- 用户等级：黄金会员（95 折，1.5 倍积分）
- 优惠券：满 300 减 50
- 商品总额：400 元
- 计算过程：
  1. 会员折扣：400 × 0.95 = 380 元
  2. 优惠券：满 300 减 50
  3. 最终应付：380 - 50 = 330 元
  4. 获得积分：330 × 1.5 = 495 积分

### 场景 3：钻石会员使用折扣券
- 用户等级：钻石会员（9 折，2 倍积分）
- 优惠券：满 200 打 8 折（最高优惠 100 元）
- 商品总额：500 元
- 计算过程：
  1. 会员折扣：500 × 0.9 = 450 元
  2. 优惠券折扣：450 × (1 - 0.8) = 90 元（未超过 100 元上限）
  3. 最终应付：450 - 90 = 360 元
  4. 获得积分：360 × 2 = 720 积分

## 六、注意事项

1. **并发控制**
   - 使用乐观锁扣减库存
   - 优惠券使用分布式锁防止超发

2. **数据一致性**
   - 订单创建失败时，回滚库存和优惠券
   - 使用@Transactional 保证事务完整性

3. **用户体验**
   - 实时计算价格变化
   - 清晰展示优惠明细
   - 提供最优优惠券推荐

4. **安全性**
   - 验证用户权限
   - 验证优惠券使用条件
   - 防止重复使用优惠券

## 七、后续优化建议

1. **智能推荐优惠券**
   - 自动为用户选择最优惠的券
   - 提示用户还差多少可用更高级别券

2. **组合优惠**
   - 支持多张优惠券叠加使用
   - 支持优惠券与促销活动同享

3. **预售和定金膨胀**
   - 定金翻倍抵扣
   - 尾款自动计算优惠

4. **团购和拼单**
   - 多人拼单享受更低价格
   - 批量购买享受批发价

5. **价格保护**
   - 购买后降价自动补差价
   - 支持保价期内申请价格保护
