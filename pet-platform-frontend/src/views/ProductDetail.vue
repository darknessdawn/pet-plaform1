<template>
    <div class="product-detail" v-if="product">
        <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ product.categoryName }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
        </el-breadcrumb>

        <el-row :gutter="40" class="product-content">
            <el-col :span="10">
                <div class="product-gallery">
                    <img :src="product.mainImage" :alt="product.name" />
                </div>
            </el-col>

            <el-col :span="14">
                <div class="product-info">
                    <h1 class="product-title">{{ product.name }}</h1>
                    <p class="product-desc">{{ product.description }}</p>

                    <div class="product-price-box">
                        <span class="price">¥{{ product.price }}</span>
                        <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
                    </div>

                    <div class="product-meta">
                        <span>销量: {{ product.sales }}</span>
                        <span>库存: {{ product.stock }}</span>
                        <span>评分: {{ product.rating }}</span>
                        <span>评价数: {{ reviewCount }}</span>
                    </div>

                    <div class="product-actions">
                        <el-input-number v-model="quantity" :min="1" :max="product.stock" />
                        <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
                        <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
                    </div>
                </div>
            </el-col>
        </el-row>

        <!-- 相似商品推荐 -->
        <div class="similar-products" v-if="similarProducts.length > 0">
            <h3>相似商品推荐</h3>
            <el-row :gutter="20">
                <el-col :span="4" v-for="item in similarProducts" :key="item.id">
                    <ProductCard :product="item" />
                </el-col>
            </el-row>
        </div>

        <!-- 商品评论 -->
        <div class="product-reviews" v-if="product">
            <h3>商品评价 ({{ reviewCount }})</h3>

            <!-- 评论列表 -->
            <div class="reviews-list" v-if="reviews.length > 0">
                <el-card v-for="review in reviews" :key="review.id" class="review-card">
                    <div class="review-header">
                        <el-avatar :size="40" :src="review.userAvatar" />
                        <div class="review-user">
                            <span class="username">{{ review.userNickname || review.username }}</span>
                            <el-rate v-model="review.rating" disabled size="small" />
                        </div>
                        <span class="review-time">{{ review.createTime }}</span>
                    </div>
                    <div class="review-content">{{ review.content }}</div>
                    <div class="review-images" v-if="review.images">
                        <img v-for="img in review.images.split(',')" :src="img" :key="img" />
                    </div>
                </el-card>
            </div>
            <el-empty v-else description="暂无评价" />

            <!-- 写评论（仅登录用户显示） -->
            <div class="add-review" v-if="userStore.isLoggedIn">
                <el-divider />
                <h4>发表评价</h4>
                <el-form :model="reviewForm" ref="reviewFormRef" :rules="reviewRules">
                    <el-form-item label="评分" prop="rating">
                        <el-rate v-model="reviewForm.rating" />
                    </el-form-item>
                    <el-form-item label="评价内容" prop="content">
                        <el-input type="textarea" v-model="reviewForm.content" :rows="3" placeholder="请输入您的评价..." />
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="submitReview" :loading="submitting">提交评价</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '../api/product'
import { getSimilarProducts, recordBehavior } from '../api/recommend'
import { addToCart as addToCartApi } from '../api/cart'
import { addReview, getProductReviews } from '../api/review'
import { useUserStore } from '../store/user'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const quantity = ref(1)
const similarProducts = ref([])

// 评论相关
const reviews = ref([])
const reviewCount = ref(0)
const submitting = ref(false)
const reviewForm = ref({
    rating: 5,
    content: ''
})
const reviewFormRef = ref(null)
const reviewRules = {
    content: [
        { required: true, message: '请输入评价内容', trigger: 'blur' },
        { min: 5, max: 500, message: '评价内容长度在5到500个字符', trigger: 'blur' }
    ]
}

// 加载商品详情和相似商品
const loadProduct = async () => {
    const id = route.params.id
    if (!id) return
    try {
        const res = await getProductDetail(id)
        product.value = res

        // 记录浏览行为（只有登录用户才记录）
        if (userStore.isLoggedIn) {
            console.log('准备记录行为，商品ID:', id)
            await recordBehavior(id, 1)
            console.log('行为记录完成')
        } else {
            console.log('用户未登录，不记录行为')
        }

        // 加载相似商品
        const similarRes = await getSimilarProducts(id, 6)
        similarProducts.value = (similarRes || []).map(item => {
            if (!item.id && item.productId) {
                item.id = item.productId
            }
            return item
        })

        // 加载评论
        await loadReviews()
    } catch (error) {
        console.error('商品加载失败，详细错误:', error)
        ElMessage.error('商品加载失败')
    }
}

// 加载评论
const loadReviews = async () => {
    try {
        const res = await getProductReviews(route.params.id)
        reviews.value = res || []
        reviewCount.value = reviews.value.length
    } catch (error) {
        console.error('加载评论失败:', error)
    }
}

// 提交评论
const submitReview = async () => {
    if (!reviewForm.value.content.trim()) {
        ElMessage.warning('请输入评价内容')
        return
    }
    submitting.value = true
    try {
        await addReview({
            productId: product.value.id,
            rating: reviewForm.value.rating,
            content: reviewForm.value.content
        })
        ElMessage.success('评价成功')
        reviewForm.value.rating = 5
        reviewForm.value.content = ''
        await loadReviews()
    } catch (error) {
        console.error('提交评论失败:', error)
        ElMessage.error('评价失败：' + (error.response?.data?.message || error.message))
    } finally {
        submitting.value = false
    }
}

// 监听路由参数变化
watch(() => route.params.id, (newId, oldId) => {
    console.log('路由ID变化:', newId)
    loadProduct()
}, { immediate: true })

const addToCart = async () => {
    try {
        await addToCartApi({
            productId: product.value.id,
            quantity: quantity.value
        })
        ElMessage.success('已加入购物车')
    } catch (error) {
        console.error('加入购物车失败:', error)
        ElMessage.error('加入购物车失败，请检查是否登录')
    }
}

const buyNow = async () => {
    if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }

    try {
        await addToCartApi({
            productId: product.value.id,
            quantity: quantity.value
        })
        ElMessage.success('商品已加入购物车，正在跳转...')
        router.push('/cart')
    } catch (error) {
        console.error('立即购买失败:', error)
        ElMessage.error('操作失败，请稍后重试')
    }
}
</script>

<style scoped>
.product-detail {
    padding: 20px 0;
}

.product-content {
    margin-top: 20px;
    background: #fff;
    padding: 30px;
    border-radius: 8px;
}

.product-gallery img {
    width: 100%;
    border-radius: 8px;
}

.product-title {
    font-size: 24px;
    margin-bottom: 15px;
}

.product-desc {
    color: #666;
    margin-bottom: 20px;
}

.product-price-box {
    margin-bottom: 20px;
}

.price {
    font-size: 32px;
    color: #ff6b6b;
    font-weight: bold;
}

.original-price {
    font-size: 18px;
    color: #999;
    text-decoration: line-through;
    margin-left: 15px;
}

.product-meta {
    display: flex;
    gap: 30px;
    color: #666;
    margin-bottom: 30px;
}

.product-actions {
    display: flex;
    gap: 15px;
    align-items: center;
}

.similar-products {
    margin-top: 40px;
}

.similar-products h3,
.product-reviews h3 {
    margin-bottom: 20px;
}

.product-reviews {
    margin-top: 40px;
    background: #fff;
    padding: 30px;
    border-radius: 8px;
}

.review-card {
    margin-bottom: 20px;
}

.review-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
}

.review-user {
    flex: 1;
}

.username {
    font-weight: bold;
    margin-right: 10px;
}

.review-time {
    color: #999;
    font-size: 14px;
}

.review-content {
    padding-left: 55px;
    color: #333;
    line-height: 1.6;
}

.review-images {
    padding-left: 55px;
    margin-top: 15px;
}

.review-images img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    margin-right: 10px;
    border-radius: 4px;
    cursor: pointer;
}

.add-review {
    margin-top: 30px;
}
</style>