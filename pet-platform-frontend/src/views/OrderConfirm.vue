<template>
  <div class="order-confirm-page">
    <h2>确认订单</h2>
    
    <el-row :gutter="20">
      <!-- 左侧：订单信息 -->
      <el-col :span="16">
        <!-- 收货地址 -->
        <el-card class="address-card">
          <template #header>
            <div class="card-header">
              <span>收货地址</span>
            </div>
          </template>
          <el-radio-group v-model="selectedAddressId">
            <el-row :gutter="20">
              <el-col :span="8" v-for="address in addressList" :key="address.id">
                <el-radio-button 
                  :value="address.id" 
                  @change="selectAddress(address)"
                  class="address-radio"
                >
                  <div class="address-item">
                    <div class="address-info">
                      {{ address.receiverName }} {{ address.phone }}
                    </div>
                    <div class="address-detail">
                      {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
                    </div>
                    <el-tag v-if="address.defaultStatus" size="small" type="success">默认</el-tag>
                  </div>
                </el-radio-button>
              </el-col>
            </el-row>
          </el-radio-group>
          <el-button type="primary" size="small" @click="showAddAddress = true" style="margin-top: 15px;">
            添加新地址
          </el-button>
        </el-card>

        <!-- 商品信息 -->
        <el-card class="product-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>商品信息</span>
            </div>
          </template>
          <el-table :data="cartItems" style="width: 100%">
            <el-table-column label="商品" min-width="300">
              <template #default="{ row }">
                <div class="product-info">
                  <img :src="row.productImage" :alt="row.productName" style="width: 60px; height: 60px; margin-right: 10px;" />
                  <span>{{ row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="{ row }">
                ¥{{ row.productPrice }}
              </template>
            </el-table-column>
            <el-table-column label="数量" width="80">
              <template #default="{ row }">
                ×{{ row.quantity }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                ¥{{ (row.productPrice * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 备注 -->
        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>订单备注</span>
            </div>
          </template>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            placeholder="选填：请输入订单备注信息"
          />
        </el-card>
      </el-col>

      <!-- 右侧：优惠和结算 -->
      <el-col :span="8">
        <el-card class="summary-card">
          <template #header>
            <div class="card-header">
              <span>订单汇总</span>
            </div>
          </template>
          
          <div class="summary-item">
            <span>商品总额：</span>
            <span class="amount">¥{{ totalAmount }}</span>
          </div>
          
          <div class="summary-item" v-if="membership.discountRate && membership.discountRate < 1">
            <span>会员折扣（{{ (membership.discountRate * 10).toFixed(1) }}折）：</span>
            <span class="amount discount">-¥{{ memberDiscountAmount }}</span>
          </div>
          
          <div class="summary-item">
            <span>优惠券：</span>
            <el-select v-model="selectedCouponId" placeholder="选择优惠券" size="small" @change="calculateTotal" clearable style="width: 150px;">
              <el-option label="不使用优惠券" :value="0" />
              <el-option
                v-for="coupon in availableCoupons"
                :key="coupon.id"
                :label="formatCouponLabel(coupon)"
                :value="coupon.id"
              />
            </el-select>
          </div>
          
          <div class="summary-item" v-if="couponDiscount > 0">
            <span>优惠金额：</span>
            <span class="amount discount">-¥{{ couponDiscount }}</span>
          </div>
          
          <el-divider />
          
          <div class="summary-total">
            <span>应付金额：</span>
            <span class="total-amount">¥{{ payAmount }}</span>
          </div>
          
          <div class="summary-points" v-if="expectedPoints > 0">
            <el-tag type="warning" size="small">预计赠送 {{ expectedPoints }} 积分</el-tag>
          </div>
          
          <el-button 
            type="primary" 
            size="large" 
            @click="submitOrder" 
            block
            style="margin-top: 20px;"
            :loading="submitting"
            :disabled="submitting"
          >
            {{ submitting ? '提交中...' : '提交订单' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加地址对话框 -->
    <el-dialog v-model="showAddAddress" title="添加收货地址" width="500px">
      <el-form :model="newAddress" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="newAddress.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="newAddress.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="所在地区">
          <el-cascader
            :options="regionOptions"
            v-model="newAddress.region"
            placeholder="请选择省市区"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="newAddress.detailAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="newAddress.defaultStatus" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddAddress = false">取消</el-button>
        <el-button type="primary" @click="handleAddAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import { getAddressList, addAddress } from '../api/address';

const router = useRouter();
const route = useRoute();

const cartItems = ref([]);
const addressList = ref([]);
const selectedAddressId = ref(null);
const remark = ref('');
const membership = ref({});
const availableCoupons = ref([]);
const selectedCouponId = ref(null);

const showAddAddress = ref(false);
const newAddress = ref({
  receiverName: '',
  phone: '',
  region: [],
  detailAddress: '',
  defaultStatus: false
});

const submitting = ref(false);

// 简单的地区数据（完整的三级结构）
const regionOptions = ref([
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '市辖区',
        label: '市辖区',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' }
        ]
      }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '市辖区',
        label: '市辖区',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '长宁区', label: '长宁区' },
          { value: '浦东新区', label: '浦东新区' }
        ]
      }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' },
          { value: '荔湾区', label: '荔湾区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '福田区', label: '福田区' },
          { value: '南山区', label: '南山区' },
          { value: '罗湖区', label: '罗湖区' },
          { value: '宝安区', label: '宝安区' }
        ]
      }
    ]
  },
  {
    value: '江苏省',
    label: '江苏省',
    children: [
      {
        value: '南京市',
        label: '南京市',
        children: [
          { value: '玄武区', label: '玄武区' },
          { value: '秦淮区', label: '秦淮区' },
          { value: '鼓楼区', label: '鼓楼区' },
          { value: '建邺区', label: '建邺区' }
        ]
      },
      {
        value: '苏州市',
        label: '苏州市',
        children: [
          { value: '姑苏区', label: '姑苏区' },
          { value: '虎丘区', label: '虎丘区' },
          { value: '吴中区', label: '吴中区' },
          { value: '相城区', label: '相城区' }
        ]
      }
    ]
  }
]);

// 计算总金额
const totalAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.productPrice * item.quantity, 0).toFixed(2);
});

// 会员折扣金额
const memberDiscountAmount = computed(() => {
  if (!membership.value.discountRate || membership.value.discountRate >= 1) return 0;
  const discount = parseFloat(totalAmount.value) * (1 - membership.value.discountRate);
  return discount.toFixed(2);
});

// 优惠券折扣金额
const couponDiscount = computed(() => {
  // selectedCouponId 为 0 或 null 时，不使用优惠券
  if (selectedCouponId.value === 0 || selectedCouponId.value === null) return 0;
  
  const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value);
  if (!coupon) return 0;
  
  const amountAfterMemberDiscount = parseFloat(totalAmount.value) - parseFloat(memberDiscountAmount.value);
  
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
  const amount = parseFloat(totalAmount.value) - parseFloat(memberDiscountAmount.value) - parseFloat(couponDiscount.value);
  return Math.max(0, amount).toFixed(2);
});

// 预计获得积分
const expectedPoints = computed(() => {
  const pointsRate = membership.value.pointsRate || 1;
  return Math.floor(parseFloat(payAmount.value) * pointsRate);
});

// 格式化优惠券标签
const formatCouponLabel = (coupon) => {
  if (!coupon || !coupon.couponName) return '未知优惠券';
  
  let label = coupon.couponName;
  
  if (coupon.type === 1) {
    // 满减券
    if (coupon.discountAmount) {
      label += ` - 减¥${coupon.discountAmount}`;
    }
  } else if (coupon.type === 2) {
    // 折扣券
    if (coupon.discountRate) {
      label += ` - ${Math.round(coupon.discountRate * 10)}折`;
    }
  }
  
  if (coupon.minPurchase) {
    label += ` (满${coupon.minPurchase}可用)`;
  }
  
  return label;
};

// 加载购物车数据
const loadCartItems = async () => {
  console.log('订单确认页 - 开始加载购物车数据...');
  try {
    const res = await request.get('/cart/list');
    console.log('订单确认页 - 购物车原始响应:', res);
    console.log('订单确认页 - 购物车数据类型:', typeof res, Array.isArray(res) ? '是数组' : '不是数组');
    
    // 检查返回的数据结构
    const cartData = Array.isArray(res) ? res : [];
    console.log('订单确认页 - 提取后的数据:', cartData);
    
    if (!Array.isArray(cartData)) {
      console.error('订单确认页 - 购物车数据不是数组:', cartData);
      cartItems.value = [];
      ElMessage.warning('购物车数据异常');
      setTimeout(() => router.push('/cart'), 1500);
      return;
    }
    
    // 详细检查每个商品的 selected 字段
    console.log('订单确认页 - 遍历检查每个商品:');
    cartData.forEach((item, index) => {
      console.log(`  商品${index}:`, {
        id: item.id,
        productId: item.productId,
        productName: item.productName,
        selected: item.selected,
        selectedType: typeof item.selected,
        quantity: item.quantity
      });
    });
    
    // 筛选选中的商品（兼容多种判断方式）
    const selectedItems = cartData.filter(item => {
      // 兼容 selected 为 0/1、false/true、null 等情况
      return item.selected === 1 || item.selected === true || (item.selected && item.selected !== 0);
    });
    
    console.log('订单确认页 - 选中的商品:', selectedItems);
    console.log('订单确认页 - 选中商品数量:', selectedItems.length);
    
    cartItems.value = selectedItems;
    
    if (cartItems.value.length === 0) {
      console.warn('订单确认页 - 没有选中的商品');
      ElMessage.warning('购物车没有选中商品，请先选择商品');
      setTimeout(() => router.push('/cart'), 1500);
    }
  } catch (error) {
    console.error('订单确认页 - 加载购物车失败:', error);
    ElMessage.error('加载购物车失败，请重试');
  }
};

// 加载地址列表
const loadAddresses = async () => {
  try {
    console.log('订单确认页 - 开始加载地址列表...');
    const res = await getAddressList();
    console.log('订单确认页 - 地址原始响应:', res);
    console.log('订单确认页 - 地址数据类型:', typeof res, Array.isArray(res) ? '是数组' : '不是数组');
    
    // request.js 已经解包了 response.data，所以 res 直接就是数组
    addressList.value = Array.isArray(res) ? res : [];
    console.log('订单确认页 - 地址列表:', addressList.value);
    console.log('订单确认页 - 地址数量:', addressList.value.length);
    
    if (addressList.value.length > 0) {
      const defaultAddr = addressList.value.find(a => a.defaultStatus) || addressList.value[0];
      console.log('订单确认页 - 选中地址:', defaultAddr);
      selectedAddressId.value = defaultAddr.id;
      console.log('订单确认页 - 选中的地址 ID:', selectedAddressId.value);
    } else {
      console.warn('订单确认页 - 没有可用地址');
      ElMessage.warning('请先添加收货地址');
    }
  } catch (error) {
    console.error('加载地址失败:', error);
    ElMessage.error('加载地址失败，请重试');
  }
};

// 加载会员信息
const loadMembership = async () => {
  try {
    const res = await request.get('/membership/my');
    membership.value = res.data || {};
  } catch (error) {
    console.error('加载会员信息失败:', error);
  }
};

// 加载可用优惠券
const loadAvailableCoupons = async () => {
  try {
    // 确保 orderAmount 是有效数字
    const amount = parseFloat(totalAmount.value);
    const orderAmount = isNaN(amount) || amount <= 0 ? 100 : amount;
    
    console.log('订单确认页 - 请求可用优惠券，金额:', orderAmount);
    const res = await request.get('/coupon/my/available', {
      params: { orderAmount }
    });
    console.log('订单确认页 - 优惠券响应:', res);
    availableCoupons.value = res.data || [];
  } catch (error) {
    console.error('订单确认页 - 加载优惠券失败:', error);
    // 不显示错误提示，允许继续操作
    availableCoupons.value = [];
  }
};

// 选择地址
const selectAddress = (address) => {
  selectedAddressId.value = address.id;
};

// 添加地址
const handleAddAddress = async () => {
  // 验证必填字段
  if (!newAddress.value.receiverName || !newAddress.value.phone) {
    ElMessage.warning('请填写收货人和手机号');
    return;
  }
  
  if (!newAddress.value.region || newAddress.value.region.length < 3) {
    ElMessage.warning('请选择完整的省市区信息');
    return;
  }
  
  if (!newAddress.value.detailAddress) {
    ElMessage.warning('请填写详细地址');
    return;
  }
  
  try {
    const addressData = {
      receiverName: newAddress.value.receiverName,
      phone: newAddress.value.phone,
      province: newAddress.value.region[0] || '',
      city: newAddress.value.region[1] || newAddress.value.region[0] || '',
      district: newAddress.value.region[2] || '',
      detailAddress: newAddress.value.detailAddress,
      defaultStatus: newAddress.value.defaultStatus ? 1 : 0
    };
    
    console.log('提交地址数据:', addressData);
    await addAddress(addressData);
    ElMessage.success('地址添加成功');
    showAddAddress.value = false;
    await loadAddresses();
  } catch (error) {
    console.error('添加地址失败:', error);
    ElMessage.error(error.response?.data?.message || '添加失败');
  }
};

// 计算总计
const calculateTotal = () => {
  // 重新计算逻辑在 computed 中已自动处理
};

// 提交订单
const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址');
    return;
  }
  
  submitting.value = true;
  
  try {
    const orderData = {
      addressId: selectedAddressId.value,
      remark: remark.value,
      couponId: selectedCouponId.value === 0 ? null : selectedCouponId.value  // 0 表示不使用优惠券
    };
    
    console.log('提交订单数据:', orderData);
    const res = await request.post('/order/create', orderData);
    
    console.log('订单创建成功，响应数据:', res);
    
    // request.js 已经解包了 response.data，所以 res 直接就是 OrderVO
    const orderId = res?.id || res?.data?.id;
    
    if (!orderId) {
      console.error('无法获取订单 ID:', res);
      ElMessage.error('订单创建失败：无法获取订单信息');
      return;
    }
    
    console.log('订单创建成功，订单 ID:', orderId);
    ElMessage.success('订单创建成功');
    
    // 跳转到订单详情页或支付页
    setTimeout(() => {
      router.push(`/order/detail/${orderId}`);
    }, 500);
  } catch (error) {
    console.error('创建订单失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '创建订单失败');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadCartItems();
  loadAddresses();
  loadMembership();
  loadAvailableCoupons();
});
</script>

<style scoped>
.order-confirm-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-card {
  margin-bottom: 20px;
}

.address-radio {
  margin-bottom: 10px;
}

.address-item {
  padding: 10px;
  text-align: left;
}

.address-info {
  font-weight: bold;
  margin-bottom: 5px;
}

.address-detail {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.product-info {
  display: flex;
  align-items: center;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.amount {
  font-weight: bold;
  color: #f56c6c;
}

.discount {
  color: #67c23a;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  margin: 20px 0;
}

.total-amount {
  color: #f56c6c;
  font-size: 24px;
}

.summary-points {
  text-align: right;
  margin-top: 10px;
}
</style>
