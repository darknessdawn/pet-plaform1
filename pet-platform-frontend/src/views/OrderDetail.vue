<template>
  <div class="order-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <!-- 订单信息 -->
      <el-descriptions v-if="order" title="订单信息" :column="2" border>
        <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="商品总额">¥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">-¥{{ order.discountAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="应付金额" :span="2">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px;">¥{{ order.payAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="product-list" v-if="order && order.orderDetails && order.orderDetails.length > 0">
        <h3>商品列表</h3>
        <el-table :data="order.orderDetails" style="width: 100%">
          <el-table-column prop="productImage" label="商品图片" width="100">
            <template #default="{ row }">
              <img :src="row.productImage" :alt="row.productName" style="width: 60px; height: 60px;" />
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" min-width="200" />
          <el-table-column prop="productPrice" label="单价" width="100">
            <template #default="{ row }">
              ¥{{ row.productPrice }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              ¥{{ (row.productPrice * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 订单操作按钮 -->
      <div class="order-actions" style="margin-top: 20px; text-align: right;">
        <el-button 
          v-if="order && order.status === 0" 
          type="primary" 
          @click="payOrder"
        >
          立即付款
        </el-button>
        <el-button 
          v-if="order && order.status === 0" 
          @click="cancelOrder"
        >
          取消订单
        </el-button>
        <el-button 
          v-if="order && order.status >= 1" 
          type="success"
          @click="viewLogistics"
        >
          查看物流
        </el-button>
      </div>

      <!-- 物流信息 -->
      <el-card v-if="showLogistics" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>物流信息</span>
            <el-button @click="showLogistics = false">关闭</el-button>
          </div>
        </template>
        <div class="logistics-content">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="物流公司">{{ logistics.company }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">{{ logistics.trackingNo }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ logistics.shipTime }}</el-descriptions-item>
          </el-descriptions>
          
          <el-divider>物流跟踪</el-divider>
          
          <el-timeline>
            <el-timeline-item 
              v-for="(item, index) in logistics.tracking" 
              :key="index" 
              :timestamp="item.time"
              placement="top"
              :type="index === 0 ? 'primary' : 'info'"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const order = ref(null);
const showLogistics = ref(false);

// 模拟物流信息
const logistics = ref({
  company: '顺丰速运',
  trackingNo: 'SF1234567890',
  shipTime: '2024-03-19 10:30:00',
  tracking: [
    {
      time: '2024-03-19 10:30:00',
      content: '已签收，签收人：本人'
    },
    {
      time: '2024-03-19 08:15:00',
      content: '快递员正在派送'
    },
    {
      time: '2024-03-19 06:00:00',
      content: '已到达【北京朝阳区分部】'
    },
    {
      time: '2024-03-18 20:30:00',
      content: '已从【上海转运中心】发出'
    },
    {
      time: '2024-03-18 14:00:00',
      content: '商家已发货'
    }
  ]
});

// 加载订单详情
const loadOrderDetail = async () => {
  loading.value = true;
  try {
    const orderId = route.params.id;
    console.log('=== 开始加载订单详情 ===');
    console.log('订单 ID:', orderId);
    
    const res = await request.get(`/order/detail/${orderId}`);
    console.log('订单详情响应:', res);
    console.log('响应数据类型:', typeof res, res === null ? 'null' : 'not null');
    console.log('响应数据是否为对象:', typeof res === 'object');
    
    // request.js 已经解包了 response.data，所以 res 直接就是 OrderVO
    if (res && typeof res === 'object') {
      order.value = res;
      console.log('✓ 订单数据已加载（方式 1）');
    } else if (res && res.data) {
      order.value = res.data;
      console.log('✓ 订单数据已加载（方式 2）');
    } else {
      console.error('✗ 未知的响应格式:', res);
      ElMessage.error('订单数据格式异常');
      return;
    }
    
    console.log('订单详情对象:', order.value);
    console.log('订单 ID:', order.value?.id);
    console.log('订单编号:', order.value?.orderNo);
    console.log('订单状态:', order.value?.status);
    console.log('订单商品列表:', order.value?.orderDetails);
    console.log('=== 订单详情加载完成 ===');
  } catch (error) {
    console.error('✗ 加载订单详情失败:', error);
    console.error('错误详情:', error.response);
    ElMessage.error(error.response?.data?.message || '加载订单详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '已付款',
    2: '已发货',
    3: '已完成',
    4: '已取消',
    5: '已关闭'
  };
  return statusMap[status] || '未知';
};

// 获取订单状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: '',
    4: 'info',
    5: 'info'
  };
  return typeMap[status] || '';
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 取消订单
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await request.put(`/order/cancel/${order.value.id}`);
    ElMessage.success('订单已取消');
    loadOrderDetail();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error);
      ElMessage.error('取消订单失败');
    }
  }
};

// 支付订单
const payOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要支付该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    console.log('开始支付订单 ID:', order.value.id);
    
    // 调用后端支付接口
    const response = await request.put(`/order/pay/${order.value.id}`, null, {
      params: {
        payType: 1 // 1-微信支付
      }
    });
    
    console.log('支付响应:', response);
    ElMessage.success('支付成功');
    
    // 强制清空 order 对象，触发 Vue 的响应式更新
    order.value = null;
    
    // 等待一小段时间，确保 DOM 完全更新
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 重新加载订单详情
    loading.value = true;
    await loadOrderDetail();
    loading.value = false;
    
    console.log('订单详情页已刷新，新状态:', order.value?.status);
    console.log('订单状态文本:', getStatusText(order.value?.status));
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error);
      console.error('错误响应数据:', error.response?.data);
      ElMessage.error(error.response?.data?.message || '支付失败');
    }
  }
};

// 查看物流
const viewLogistics = () => {
  showLogistics.value = true;
  console.log('显示物流信息');
};

// 返回上一页

onMounted(() => {
  loadOrderDetail();
});
</script>

<style scoped>
.order-detail-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-list h3 {
  margin-bottom: 15px;
}

.order-actions {
  margin-top: 20px;
}
</style>
