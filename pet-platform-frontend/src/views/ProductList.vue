<template>
  <div class="product-list-page">
    <div class="page-header">
      <h2>全部商品</h2>
      <p v-if="currentCategory">当前分类：{{ currentCategory }}</p>
    </div>

    <div class="filter-bar">
      <div class="category-filter">
        <span class="label">分类：</span>
        <el-tag
          v-for="cat in categories"
          :key="cat.id"
          :type="selectedCategory === cat.id ? 'primary' : ''"
          style="margin-right: 10px; cursor: pointer;"
          @click="selectCategory(cat.id)"
        >
          {{ cat.name }}
        </el-tag>
        <el-tag
          :type="!selectedCategory ? 'primary' : ''"
          style="cursor: pointer;"
          @click="selectCategory(null)"
        >
          全部
        </el-tag>
      </div>
    </div>

    <div class="products-container" v-loading="loading">
      <el-row v-if="products.length > 0" :gutter="20">
        <el-col :span="6" v-for="product in products" :key="product.id">
          <ProductCard :product="product" />
        </el-col>
      </el-row>

      <el-empty v-else description="暂无商品" />
    </div>

    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="loadProducts"
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductList, getCategoryList } from '../api/product'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()

const products = ref([])
const categories = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedCategory = ref(null)
const currentCategory = ref(null)

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    // 只取一级分类
    categories.value = (res || []).filter(c => c.level === 1)
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 加载商品
const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    
    console.log('请求参数:', params)
    const res = await getProductList(params)
    console.log('后端返回:', res)
    
    // 后端返回的是 {list: [], total: number}
    products.value = res.list || []
    total.value = res.total || 0
    
    console.log('商品列表:', products.value)
    console.log('总数:', total.value)
  } catch (error) {
    console.error('加载商品失败', error)
  } finally {
    loading.value = false
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
  currentPage.value = 1
  
  // 更新 URL 参数
  const query = {}
  if (categoryId) {
    query.categoryId = categoryId
  }
  router.push({ path: '/product/list', query })
}

// 监听路由变化
watch(() => route.query.categoryId, async (newVal) => {
  if (newVal) {
    selectedCategory.value = parseInt(newVal)
    // 等待分类加载完成后再查找分类名称
    if (categories.value.length === 0) {
      await loadCategories()
    }
    const category = categories.value.find(c => c.id === selectedCategory.value)
    currentCategory.value = category ? category.name : ''
  } else {
    selectedCategory.value = null
    currentCategory.value = ''
  }
  loadProducts()
}, { immediate: true })

onMounted(() => {
  // 如果 watch 已经处理了 URL 参数，就不需要再调用 loadProducts
  // 只加载分类即可
  loadCategories()
})
</script>

<style scoped>
.product-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.page-header p {
  color: #666;
  font-size: 14px;
}

.filter-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.category-filter .label {
  font-weight: bold;
  margin-right: 15px;
  color: #666;
}

.products-container {
  min-height: 400px;
}

.pagination {
  margin-top: 30px;
  text-align: center;
}
</style>
