<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon size="32" color="#ff6b6b"><Shop /></el-icon>
          <span>宠物用品商城</span>
        </div>
        
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="nav-menu">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo.avatar" />
                <span>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item divided command="membership">会员中心</el-dropdown-item>
                  <el-dropdown-item command="coupon">优惠券</el-dropdown-item>
                  <el-dropdown-item command="points">积分商城</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-badge :value="cartCount" class="cart-badge" @click="$router.push('/cart')">
              <el-icon size="24"><ShoppingCart /></el-icon>
            </el-badge>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <el-main class="main">
      <router-view />
    </el-main>
    
    <el-footer class="footer">
      <p>宠物用品交易平台 - 基于协同过滤推荐算法</p>
      <p>技术栈: Spring Boot + Vue3 + MySQL + Redis</p>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCartCount as getCartCountApi } from '../api/cart'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const cartCount = ref(0)

// 获取购物车数量
const loadCartCount = async () => {
  if (userStore.isLoggedIn) {
    try {
      const res = await getCartCountApi()
      cartCount.value = res || 0
    } catch (error) {
      console.error('获取购物车数量失败:', error)
    }
  } else {
    cartCount.value = 0
  }
}

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (newVal) => {
  loadCartCount()
}, { immediate: true })

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/', query: { keyword: searchKeyword.value } })
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/order')
      break
    case 'membership':
      router.push('/membership')
      break
    case 'coupon':
      router.push('/coupon')
      break
    case 'points':
      router.push('/points')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        ElMessage.success('退出成功')
        router.push('/')
      })
      break
  }
}

onMounted(() => {
  loadCartCount()
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
  cursor: pointer;
}

.search-box {
  flex: 1;
  max-width: 500px;
  margin: 0 40px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.cart-badge {
  cursor: pointer;
}

.main {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
}

.footer {
  background: #333;
  color: #fff;
  text-align: center;
  padding: 20px;
}

.footer p {
  margin: 5px 0;
}
</style>
