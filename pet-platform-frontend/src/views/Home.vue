<template>
  <div class="home">
    <!-- 广告位 -->
    <div class="banner">
      <el-carousel height="300px">
        <el-carousel-item>
          <div class="banner-item" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <div class="banner-content">
              <h2>🐾 宠物领养</h2>
              <p>给流浪的它们一个温暖的家，让爱不再流浪</p>
            </div>
          </div>
        </el-carousel-item>
        <el-carousel-item>
          <div class="banner-item" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <div class="banner-content">
              <h2>🏷️ 宠物用品特惠</h2>
              <p>全场满 199 减 50，限时优惠中</p>
            </div>
          </div>
        </el-carousel-item>
        <el-carousel-item>
          <div class="banner-item" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <div class="banner-content">
              <h2>❤️ 宠物交友</h2>
              <p>为您的爱宠寻找志同道合的小伙伴</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 分类展示 -->
    <div class="section">
      <div class="section-header">
        <h3>
          <el-icon><Grid /></el-icon>
          商品分类
        </h3>
      </div>
      <div class="category-list">
        <div 
          v-for="category in categories" 
          :key="category.id" 
          class="category-item"
          @click="filterByCategory(category.id)"
        >
          <el-icon size="32"><FolderOpened /></el-icon>
          <span>{{ category.name }}</span>
        </div>
      </div>
    </div>

    <!-- 个性化推荐 -->
    <div class="section" v-if="userStore.isLoggedIn && personalRecommendations.length > 0">
      <div class="section-header">
        <h3>
          <el-icon><StarFilled /></el-icon>
          为您推荐
        </h3>
        <span class="subtitle">基于您的浏览和购买历史智能推荐</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in personalRecommendations" :key="product.id">
          <ProductCard :product="product" />
        </el-col>
      </el-row>
    </div>

    <!-- 热门商品 -->
    <div class="section">
      <div class="section-header">
        <h3>
          <el-icon><TrendCharts /></el-icon>
          热门商品
        </h3>
        <el-link type="primary" @click="$router.push('/product/list')">查看更多</el-link>
      </div>
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in hotProducts" :key="product.id">
          <ProductCard :product="product" />
        </el-col>
      </el-row>
    </div>

    <!-- 新品推荐 -->
    <div class="section" ref="newSection">
      <div class="section-header">
        <h3>
          <el-icon><GoodsFilled /></el-icon>
          新品上市
        </h3>
        <el-link type="primary" @click="$router.push('/product/list')">查看更多</el-link>
      </div>
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in newProducts" :key="product.id">
          <ProductCard :product="product" />
        </el-col>
      </el-row>
    </div>

    <!-- 推荐算法说明 -->
    <div class="section algorithm-info">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>智能推荐系统</span>
          </div>
        </template>
        <div class="algorithm-content">
          <div class="algo-item">
            <el-icon size="40" color="#ff6b6b"><UserFilled /></el-icon>
            <h4>UserCF</h4>
            <p>基于用户的协同过滤，找到与您兴趣相似的用户</p>
          </div>
          <div class="algo-item">
            <el-icon size="40" color="#4ecdc4"><Goods /></el-icon>
            <h4>ItemCF</h4>
            <p>基于物品的协同过滤，推荐与您历史喜好相似的商品</p>
          </div>
          <div class="algo-item">
            <el-icon size="40" color="#45b7d1"><DataAnalysis /></el-icon>
            <h4>SVD矩阵分解</h4>
            <p>降维处理稀疏矩阵，挖掘潜在兴趣特征</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'
import { getHotProducts, getNewProducts, getCategoryList } from '../api/product'
import { getPersonalRecommendations } from '../api/recommend'
import ProductCard from '../components/ProductCard.vue'

const router = useRouter()
const userStore = useUserStore()

const hotProducts = ref([])
const newProducts = ref([])
const personalRecommendations = ref([])
const categories = ref([])
const newSection = ref(null)

const loadData = async () => {
  try {
    // 加载热门商品
    const hotRes = await getHotProducts(8)
    hotProducts.value = hotRes || []

    // 加载新品
    const newRes = await getNewProducts(8)
    newProducts.value = newRes || []

    // 加载分类
    const catRes = await getCategoryList()
    categories.value = (catRes || []).slice(0, 8)

    // 加载个性化推荐（如果已登录）
    if (userStore.isLoggedIn) {
      try {
        const personalRes = await getPersonalRecommendations(4)
        personalRecommendations.value = personalRes || []
      } catch (error) {
        console.log('个性化推荐加载失败', error)
        personalRecommendations.value = []
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    hotProducts.value = []
    newProducts.value = []
    categories.value = []
  }
}

const scrollToProducts = () => {
  window.scrollTo({ top: 400, behavior: 'smooth' })
}

const scrollToNew = () => {
  newSection.value?.scrollIntoView({ behavior: 'smooth' })
}

const filterByCategory = (categoryId) => {
  // 跳转到商品列表页并带上分类参数
  router.push({ path: '/product/list', query: { categoryId } })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home {
  padding-bottom: 40px;
}

.banner {
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.banner-content {
  text-align: center;
  z-index: 1;
}

.banner-content h2 {
  font-size: 42px;
  margin-bottom: 15px;
  font-weight: bold;
  text-shadow: 2px 2px 8px rgba(0,0,0,0.3);
}

.banner-content p {
  font-size: 20px;
  margin-bottom: 25px;
  opacity: 0.95;
  text-shadow: 1px 1px 4px rgba(0,0,0,0.3);
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  color: #333;
}

.subtitle {
  color: #999;
  font-size: 14px;
  margin-left: 10px;
}

.category-list {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 15px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.category-item span {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.algorithm-info {
  margin-top: 40px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.algorithm-content {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.algo-item {
  text-align: center;
  padding: 20px;
}

.algo-item h4 {
  margin: 10px 0;
  color: #333;
}

.algo-item p {
  color: #666;
  font-size: 14px;
  max-width: 200px;
}
</style>
