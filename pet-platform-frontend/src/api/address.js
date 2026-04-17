import request from '../utils/request'

// 获取地址列表
export const getAddressList = () => {
  return request.get('/address/list')
}

// 获取地址详情
export const getAddressDetail = (id) => {
  return request.get(`/address/detail/${id}`)
}

// 添加地址
export const addAddress = (data) => {
  return request.post('/address/add', data)
}

// 更新地址
export const updateAddress = (id, data) => {
  return request.put(`/address/update/${id}`, data)
}

// 删除地址
export const deleteAddress = (id) => {
  return request.delete(`/address/delete/${id}`)
}

// 设置默认地址
export const setDefaultAddress = (id) => {
  return request.put(`/address/default/${id}`)
}
