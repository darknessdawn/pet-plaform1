<template>
    <div class="product-card" @click="goToDetail">
        <div class="product-image">
            <img :src="product?.mainImage || '/default-product.png'" :alt="product?.name" />
            <div class="product-tags" v-if="product?.originalPrice && product.originalPrice > product.price">
                <span class="tag discount">特惠</span>
            </div>
        </div>
        <div class="product-info">
            <h4 class="product-name">{{ product?.name }}</h4>
            <p class="product-desc" v-if="product?.description">{{ product.description.slice(0, 50) }}...</p>
            <div class="product-meta">
                <span class="price">¥{{ product?.price }}</span>
                <span class="original-price" v-if="product?.originalPrice">¥{{ product.originalPrice }}</span>
                <span class="sales">已售 {{ product?.sales || 0 }}</span>
            </div>
            <div class="product-rating" v-if="product?.rating">
                <el-rate :model-value="product.rating" disabled show-score text-color="#ff9900" />
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
    product: {
        type: Object,
        required: true
    }
})

const router = useRouter()

const goToDetail = () => {
    console.log('点击商品ID:', props.product.id)  // 调试用
    router.push(`/product/${props.product.id}`)
}
</script>

<style scoped>
.product-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.product-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.product-image {
    position: relative;
    width: 100%;
    height: 200px;
    overflow: hidden;
}

.product-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
}

.product-card:hover .product-image img {
    transform: scale(1.05);
}

.product-tags {
    position: absolute;
    top: 10px;
    left: 10px;
}

.tag {
    display: inline-block;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    color: #fff;
}

.tag.discount {
    background: #ff6b6b;
}

.product-info {
    padding: 15px;
}

.product-name {
    font-size: 16px;
    color: #333;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-desc {
    font-size: 13px;
    color: #999;
    margin-bottom: 10px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-meta {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
}

.price {
    font-size: 20px;
    font-weight: bold;
    color: #ff6b6b;
}

.original-price {
    font-size: 14px;
    color: #999;
    text-decoration: line-through;
}

.sales {
    font-size: 12px;
    color: #999;
    margin-left: auto;
}

.product-rating {
    display: flex;
    align-items: center;
}
</style>