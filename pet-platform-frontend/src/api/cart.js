import request from '../utils/request'

// 获取购物车列表
export const getCartList = () => {
  return request.get('/cart/list')
}

// 添加商品到购物车
export const addToCart = (data) => {
  return request.post('/cart/add', null, {
    params: {
      productId: data.productId,
      quantity: data.quantity
    }
  })
}

// 更新购物车商品数量
export const updateCartItem = (id, quantity) => {
  return request.put(`/cart/update/${id}`, { quantity })
}

// 删除购物车商品
export const deleteCartItem = (id) => {
  return request.delete(`/cart/delete/${id}`)
}

// 清空购物车
export const clearCart = () => {
  return request.delete('/cart/clear')
}

// 获取购物车数量
export const getCartCount = () => {
  return request.get('/cart/count')
}

// 选择/取消选择购物车商品
export const selectCartItem = (id, selected) => {
  return request.put(`/cart/select/${id}`, null, {
    params: { selected }
  })
}
