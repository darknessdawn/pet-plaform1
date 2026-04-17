<template>
  <div class="coupon-page">
    <h2>优惠券中心</h2>
    
    <!-- 可领取优惠券 -->
    <div class="coupon-section">
      <h3>可领取优惠券</h3>
      <el-row :gutter="20">
        <el-col :span="8" v-for="coupon in availableCoupons" :key="coupon.id">
          <el-card class="coupon-card">
            <div class="coupon-content">
              <div class="coupon-info">
                <div class="coupon-amount" v-if="coupon.type === 1">
                  ¥{{ coupon.discountAmount }}
                </div>
                <div class="coupon-amount" v-else>
                  {{ (coupon.discountRate * 10).toFixed(1) }}折
                </div>
                <div class="coupon-name">{{ coupon.name }}</div>
                <div class="coupon-condition">
                  <span v-if="coupon.minPurchase">满{{ coupon.minPurchase }}可用</span>
                  <span v-else>无门槛</span>
                </div>
                <div class="coupon-validity">
                  有效期至 {{ formatDate(coupon.validEnd) }}
                </div>
              </div>
              <el-button 
                type="primary" 
                @click="receiveCoupon(coupon.id)"
                :disabled="coupon.issuedCount >= coupon.totalCount"
              >
                {{ coupon.issuedCount >= coupon.totalCount ? '已领完' : '立即领取' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 我的优惠券 -->
    <div class="coupon-section">
      <h3>我的优惠券</h3>
      <el-tabs v-model="myCouponTab">
        <el-tab-pane label="未使用" name="0"></el-tab-pane>
        <el-tab-pane label="已使用" name="1"></el-tab-pane>
        <el-tab-pane label="已过期" name="2"></el-tab-pane>
      </el-tabs>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="coupon in myCoupons" :key="coupon.id">
          <el-card class="coupon-card">
            <div class="coupon-content">
              <div class="coupon-info">
                <div class="coupon-amount" v-if="coupon.type === 1">
                  ¥{{ coupon.discountAmount }}
                </div>
                <div class="coupon-amount" v-else>
                  {{ (coupon.discountRate * 10).toFixed(1) }}折
                </div>
                <div class="coupon-name">{{ coupon.couponName }}</div>
                <div class="coupon-condition">
                  <span v-if="coupon.minPurchase">满{{ coupon.minPurchase }}可用</span>
                  <span v-else>无门槛</span>
                </div>
                <div class="coupon-status" :class="`status-${coupon.status}`">
                  {{ getStatusText(coupon.status) }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-empty v-if="myCoupons.length === 0" description="暂无优惠券" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

const availableCoupons = ref([]);
const myCoupons = ref([]);
const myCouponTab = ref('0');

// 加载可领取优惠券
const loadAvailableCoupons = async () => {
  try {
    const res = await request.get('/coupon/template/list');
    availableCoupons.value = res.data || [];
  } catch (error) {
    console.error('加载优惠券列表失败:', error);
  }
};

// 加载我的优惠券
const loadMyCoupons = async () => {
  try {
    const res = await request.get('/coupon/my/list', {
      params: { status: parseInt(myCouponTab.value) }
    });
    myCoupons.value = res.data || [];
  } catch (error) {
    console.error('加载我的优惠券失败:', error);
  }
};

// 领取优惠券
const receiveCoupon = async (templateId) => {
  try {
    await request.post(`/coupon/receive/${templateId}`);
    ElMessage.success('领取成功');
    loadAvailableCoupons();
    loadMyCoupons();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '领取失败');
  }
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
};

// 获取状态文本
const getStatusText = (status) => {
  switch(status) {
    case 0: return '未使用';
    case 1: return '已使用';
    case 2: return '已过期';
    default: return '未知';
  }
};

onMounted(() => {
  loadAvailableCoupons();
  loadMyCoupons();
});
</script>

<style scoped>
.coupon-page {
  padding: 20px;
}

.coupon-section {
  margin-bottom: 30px;
}

.coupon-card {
  margin-bottom: 20px;
}

.coupon-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coupon-info {
  flex: 1;
}

.coupon-amount {
  font-size: 32px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 10px;
}

.coupon-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.coupon-condition {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.coupon-validity {
  font-size: 12px;
  color: #c0c4cc;
}

.coupon-status {
  font-size: 14px;
  font-weight: bold;
  margin-top: 10px;
}

.status-0 {
  color: #67c23a;
}

.status-1 {
  color: #909399;
}

.status-2 {
  color: #f56c6c;
}
</style>
