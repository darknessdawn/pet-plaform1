import request from '../utils/request'

// 添加评论
export const addReview = (data) => {
    return request.post('/review/add', data)
}

// 更新评论
export const updateReview = (reviewId, data) => {
    return request.put(`/review/${reviewId}`, data)
}

// 删除评论
export const deleteReview = (reviewId) => {
    return request.delete(`/review/${reviewId}`)
}

// 获取商品评论列表
export const getProductReviews = (productId) => {
    return request.get(`/review/product/${productId}`)
}

// 获取当前用户的评论列表
export const getUserReviews = () => {
    return request.get('/review/user')
}