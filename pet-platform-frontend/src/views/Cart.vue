<template>
  <div class="cart-page">
    <h2>购物车</h2>
    
    <div v-if="loading" class="loading-cart">
      <el-empty description="加载中..." />
    </div>
    
    <div v-else-if="cartList.length === 0" class="empty-cart">
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="$router.push('/')">去购物</el-button>
      </el-empty>
    </div>
    
    <div v-else class="cart-content">
      <el-table :data="cartList" style="width: 100%">
        <el-table-column width="60">
          <template #default="{ row }">
            <el-checkbox v-model="row.selected" @change="updateSelection(row)" />
          </template>
        </el-table-column>
        
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="product-info">
              <img :src="row.productImage" :alt="row.productName" />
              <span>{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="单价" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.productPrice }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="数量" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="row.stock" size="small" @change="updateQuantity(row)" />
          </template>
        </el-table-column>
        
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="deleteItem(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="cart-footer">
        <div class="select-all">
          <el-checkbox v-model="allSelected" @change="toggleSelectAll">全选</el-checkbox>
        </div>
        <div class="cart-summary">
          <span>已选 {{ selectedCount }} 件商品</span>
          <span class="total-price">合计: ¥{{ totalPrice }}</span>
          <el-button type="danger" size="large" @click="checkout">去结算</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCartList, updateCartItem, deleteCartItem, selectCartItem } from '../api/cart'
import { createOrder } from '../api/order'
import { getAddressList } from '../api/address'

const router = useRouter()
const cartList = ref([])
const loading = ref(false)

const allSelected = computed({
  get: () => cartList.value.length > 0 && cartList.value.every(item => item.selected),
  set: (val) => toggleSelectAll(val)
})

const selectedCount = computed(() => cartList.value.filter(item => item.selected).length)

const totalPrice = computed(() => {
  return cartList.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + parseFloat(item.productPrice * item.quantity), 0)
    .toFixed(2)
})

const loadCart = async () => {
  loading.value = true
  try {
    console.log('开始加载购物车数据...')
    const res = await getCartList()
    console.log('购物车响应:', res)
    // request.js 已经返回了 data 字段，所以直接使用 res
    cartList.value = Array.isArray(res) ? res : []
    // 为每个商品添加 selected 属性
    cartList.value.forEach(item => {
      if (item.selected === undefined || item.selected === null) {
        item.selected = true
      } else {
        item.selected = item.selected !== 0
      }
    })
    console.log('购物车数据:', cartList.value)
  } catch (error) {
    console.error('加载购物车失败:', error)
    ElMessage.error('加载购物车失败，请确保已登录')
  } finally {
    loading.value = false
  }
}

const updateSelection = async (row) => {
  console.log('更新选择:', row)
  try {
    // 使用专门的 select API 而不是 update API
    await selectCartItem(row.id, row.selected ? 1 : 0)
  } catch (error) {
    console.error('更新选择失败:', error)
  }
}

const updateQuantity = async (row) => {
  try {
    await updateCartItem(row.id, row.quantity)
    row.totalPrice = (row.productPrice * row.quantity).toFixed(2)
  } catch (error) {
    console.error('更新数量失败:', error)
    ElMessage.error('更新数量失败')
  }
}

const deleteItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCartItem(id)
    cartList.value = cartList.value.filter(item => item.id !== id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const toggleSelectAll = (val) => {
  cartList.value.forEach(item => item.selected = val)
}

const checkout = async () => {
  console.log('点击去结算按钮')
  const selectedItems = cartList.value.filter(item => item.selected)
  console.log('选中的商品:', selectedItems)
  
  if (selectedItems.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  console.log('跳转到订单确认页:', selectedItems.length, '件商品')
  // 直接跳转到订单确认页
  router.push('/order/confirm')
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-page {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.empty-cart {
  padding: 60px 0;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-info img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.price {
  color: #ff6b6b;
  font-weight: bold;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-price {
  font-size: 24px;
  color: #ff6b6b;
  font-weight: bold;
}
</style>
