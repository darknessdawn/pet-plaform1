<template>
  <div class="order-page">
    <h2>我的订单</h2>
    
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部订单" name="all"></el-tab-pane>
      <el-tab-pane label="待付款" name="unpaid"></el-tab-pane>
      <el-tab-pane label="待发货" name="unshipped"></el-tab-pane>
      <el-tab-pane label="待收货" name="unreceived"></el-tab-pane>
      <el-tab-pane label="已完成" name="completed"></el-tab-pane>
    </el-tabs>
    
    <div class="order-list">
      <!-- 刷新按钮 -->
      <div style="margin-bottom: 10px; text-align: right;">
        <el-button size="small" @click="forceRefresh()" icon="Refresh">刷新列表</el-button>
      </div>
      
      <el-empty v-if="orderList.length === 0" description="暂无订单" />
      
      <div v-else class="order-items">
        <el-card v-for="(order, index) in orderList" :key="order.id + '-' + index" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </div>
          
          <div class="order-products">
            <div v-for="item in order.orderDetails" :key="item.id" class="product-item">
              <img :src="item.productImage" :alt="item.productName" />
              <div class="product-info">
                <h4>{{ item.productName }}</h4>
                <p>¥{{ item.productPrice }} x {{ item.quantity }}</p>
              </div>
              <div class="product-total">¥{{ item.totalAmount }}</div>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              <span>共 {{ order.orderDetails?.length || 0 }} 件商品</span>
              <span class="total-amount">实付: ¥{{ order.payAmount }}</span>
            </div>
            <div class="order-actions">
              <el-button v-if="order.status === 0" type="primary" @click="payOrder(order)">立即付款</el-button>
              <el-button v-if="order.status === 0" @click="cancelOrder(order)">取消订单</el-button>
              
              <!-- 待发货按钮（管理员可见，这里模拟展示） -->
              <el-button v-if="order.status === 1" type="warning" @click="shipOrder(order)">我要发货</el-button>
              
              <!-- 待收货按钮 -->
              <el-button v-if="order.status === 2" type="primary" @click="confirmReceive(order)">确认收货</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getOrderList, 
  cancelOrder as cancelOrderApi, 
  payOrder as payOrderApi, 
  confirmReceive as confirmReceiveApi,
  shipOrder as shipOrderApi
} from '../api/order'

const activeTab = ref('all')
const orderList = ref([])
const refreshTimer = ref(null) // 定时器引用

const loadOrders = async () => {
  try {
    let status = null
    if (activeTab.value === 'unpaid') {
      status = 0
    } else if (activeTab.value === 'unshipped') {
      status = 1
    } else if (activeTab.value === 'unreceived') {
      status = 2
    } else if (activeTab.value === 'completed') {
      status = 3
    }
    
    console.log('📋 加载订单，status:', status)
    const res = await getOrderList(status)
    console.log('✅ 订单响应:', res)
    orderList.value = Array.isArray(res) ? res : []
    console.log('📦 订单列表数量:', orderList.value.length)
  } catch (error) {
    console.error('❌ 加载订单失败:', error)
    ElMessage.error('加载订单失败，请确保已登录')
    orderList.value = []
  }
}

// 定时刷新订单列表（每 5 秒）
const startAutoRefresh = () => {
  stopAutoRefresh() // 先清除之前的定时器
  refreshTimer.value = setInterval(() => {
    console.log('⏰ 自动刷新订单列表...')
    loadOrders()
  }, 5000)
  console.log('✅ 自动刷新已启动，每 5 秒刷新一次')
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
    console.log('⏹️ 自动刷新已停止')
  }
}

const handleTabChange = (tab) => {
  console.log('📑 切换标签到:', tab)
  // 先清空列表，强制 Vue 重新渲染
  orderList.value = []
  setTimeout(() => {
    loadOrders()
  }, 100)
}

// 强制刷新函数
const forceRefresh = async () => {
  console.log('🔄 强制刷新订单列表')
  // 先清空列表
  orderList.value = []
  await new Promise(resolve => setTimeout(resolve, 200))
  // 重新加载
  await loadOrders()
  console.log('✅ 强制刷新完成，当前数量:', orderList.value.length)
}

const payOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要支付该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log('开始支付订单 ID:', order.id)
    
    // 调用后端支付接口
    const response = await payOrderApi(order.id, 1) // 1 表示微信支付
    console.log('支付响应:', response)
    
    ElMessage.success('支付成功')
    
    // 刷新列表，切换到"待发货"标签
    await refreshAfterAction('unshipped')
  } catch (error) {
    console.error('支付完整错误对象:', error)
    console.error('错误响应数据:', error.response)
    console.error('错误数据 body:', error.response?.data)
    
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '支付失败'
      ElMessage.error(`支付失败：${errorMsg}`)
    }
  }
}

// 统一的订单操作刷新函数
const refreshAfterAction = async (targetTab = null) => {
  console.log('🔄 开始刷新订单列表...')
  
  // 先停止自动刷新
  stopAutoRefresh()
  
  // 清空列表，强制 Vue 重新渲染
  orderList.value = []
  
  // 等待 DOM 更新
  await new Promise(resolve => setTimeout(resolve, 300))
  
  // 如果指定了目标标签页，切换过去
  if (targetTab) {
    activeTab.value = targetTab
  }
  
  // 重新加载订单
  await loadOrders()
  console.log('✅ 订单列表刷新完成，当前数量:', orderList.value.length, '标签:', activeTab.value)
  
  // 重启自动刷新
  startAutoRefresh()
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log('取消订单，ID:', order.id)
    await cancelOrderApi(order.id)
    
    ElMessage.success('订单已取消')
    
    // 刷新列表，保持在当前标签页
    await refreshAfterAction()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error(error.response?.data?.message || '取消订单失败')
    }
  }
}

const confirmReceive = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    console.log('确认收货，订单 ID:', order.id)
    await confirmReceiveApi(order.id)
    
    ElMessage.success('确认收货成功')
    
    // 刷新列表，切换到"已完成"标签
    await refreshAfterAction('completed')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error(error.response?.data?.message || '确认收货失败')
    }
  }
}

// 发货功能
const shipOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log('开始发货，订单 ID:', order.id)
    
    // 调用后端发货接口
    await shipOrderApi(order.id)
    
    ElMessage.success('发货成功')
    
    // 刷新列表，切换到"待收货"标签
    await refreshAfterAction('unreceived')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error(error.response?.data?.message || '发货失败')
    }
  }
}

onMounted(() => {
  loadOrders()
  startAutoRefresh() // 启动自动刷新
})

// 组件卸载时清除定时器
onUnmounted(() => {
  stopAutoRefresh()
})

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '已关闭'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',    // 待付款 - 橙色
    1: 'primary',    // 待发货 - 蓝色
    2: 'success',    // 待收货 - 绿色
    3: 'info',       // 已完成 - 灰色
    4: 'danger',     // 已取消 - 红色
    5: 'info'        // 已关闭 - 灰色
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped>
.order-page {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.order-list {
  margin-top: 20px;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
}

.order-no {
  color: #666;
}

.order-status {
  color: #ff6b6b;
  font-weight: bold;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
}

.product-item img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin-bottom: 8px;
}

.product-info p {
  color: #999;
}

.product-total {
  font-weight: bold;
  color: #ff6b6b;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
}

.order-total {
  display: flex;
  align-items: center;
  gap: 15px;
}

.total-amount {
  font-size: 18px;
  color: #ff6b6b;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 10px;
}
</style>
