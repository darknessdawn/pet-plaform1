import request from '../utils/request'

// 获取订单列表
export const getOrderList = (status) => {
  return request.get('/order/list', { params: { status } })
}

// 获取订单详情
export const getOrderDetail = (orderId) => {
  return request.get(`/order/detail/${orderId}`)
}

// 创建订单
export const createOrder = (data) => {
  return request.post('/order/create', data)
}

// 取消订单
export const cancelOrder = (orderId) => {
  return request.put(`/order/cancel/${orderId}`)
}

// 支付订单
export const payOrder = (orderId, payType) => {
  return request.put(`/order/pay/${orderId}`, null, {
    params: { payType }
  })
}

// 确认收货
export const confirmReceive = (orderId) => {
  return request.put(`/order/confirm/${orderId}`)
}

// 申请退款
export const applyRefund = (orderId, reason) => {
  return request.put(`/order/refund/${orderId}`, null, {
    params: { reason }
  })
}

// 发货订单
export const shipOrder = (orderId) => {
  return request.put(`/order/ship/${orderId}`)
}
