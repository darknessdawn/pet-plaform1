<template>
  <div class="points-mall">
    <h2>积分商城</h2>
    
    <!-- 用户积分信息 -->
    <el-card class="points-info-card">
      <div class="points-header">
        <div class="points-title">我的积分</div>
        <div class="points-value">{{ membership.points || 0 }}</div>
        <div class="points-desc">可用积分</div>
      </div>
    </el-card>

    <!-- 积分商品列表 -->
    <div class="products-section">
      <h3>可兑换商品</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" shadow="hover">
            <div class="product-image">
              <el-image 
                :src="product.image || 'https://via.placeholder.com/200'" 
                fit="cover"
                style="width: 100%; height: 200px;"
              />
            </div>
            <div class="product-info">
              <div class="product-name">{{ product.name }}</div>
              <div class="product-description">{{ product.description }}</div>
              <div class="product-points">
                <span class="points-price">{{ product.pointsPrice }} 积分</span>
                <span class="cash-price" v-if="product.cashPrice">
                  ¥{{ product.cashPrice }}
                </span>
              </div>
              <div class="product-stock">库存：{{ product.stock }}</div>
              <el-button 
                type="primary" 
                @click="exchangeProduct(product.id)"
                :disabled="membership.points < product.pointsPrice || product.stock <= 0"
                block
              >
                {{ membership.points < product.pointsPrice ? '积分不足' : '立即兑换' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-empty v-if="products.length === 0" description="暂无积分商品" />
    </div>

    <!-- 兑换记录 -->
    <div class="orders-section">
      <h3>兑换记录</h3>
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column label="兑换积分" width="150">
          <template #default="{ row }">
            {{ row.pointsPrice }} 积分
          </template>
        </el-table-column>
        <el-table-column label="现金价格" width="150">
          <template #default="{ row }">
            ¥{{ row.cashPrice }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已完成' : '待发货' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

const membership = ref({});
const products = ref([]);
const orders = ref([]);

// 加载会员信息
const loadMembershipInfo = async () => {
  try {
    const res = await request.get('/membership/my');
    membership.value = res.data || {};
  } catch (error) {
    console.error('加载会员信息失败:', error);
  }
};

// 加载积分商品
const loadProducts = async () => {
  try {
    const res = await request.get('/points/products');
    products.value = res.data || [];
  } catch (error) {
    console.error('加载积分商品失败:', error);
  }
};

// 加载兑换记录
const loadOrders = async () => {
  try {
    const res = await request.get('/points/my/orders');
    orders.value = res.data || [];
  } catch (error) {
    console.error('加载兑换记录失败:', error);
  }
};

// 兑换商品
const exchangeProduct = async (productId) => {
  try {
    await ElMessageBox.confirm('确认使用积分兑换该商品吗？', '兑换确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await request.post(`/points/exchange/${productId}`);
    ElMessage.success('兑换成功');
    loadMembershipInfo();
    loadProducts();
    loadOrders();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '兑换失败');
    }
  }
};

onMounted(() => {
  loadMembershipInfo();
  loadProducts();
  loadOrders();
});
</script>

<style scoped>
.points-mall {
  padding: 20px;
}

.points-info-card {
  margin-bottom: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.points-header {
  text-align: center;
  padding: 20px;
}

.points-title {
  font-size: 18px;
  opacity: 0.9;
}

.points-value {
  font-size: 48px;
  font-weight: bold;
  margin: 10px 0;
}

.points-desc {
  font-size: 14px;
  opacity: 0.8;
}

.products-section {
  margin-bottom: 30px;
}

.product-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-info {
  padding: 15px 0 0 0;
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.product-description {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
  height: 40px;
  overflow: hidden;
}

.product-points {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 10px;
}

.points-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.cash-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}

.product-stock {
  font-size: 12px;
  color: #c0c4cc;
  margin-bottom: 10px;
}

.orders-section {
  margin-top: 30px;
}
</style>
