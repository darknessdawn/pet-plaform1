import request from '../utils/request'

export const getProductList = (params) => {
  return request.get('/product/list', { params })
}

export const getProductDetail = (id) => {
  return request.get(`/product/detail/${id}`)
}

export const getHotProducts = (limit = 10) => {
  return request.get('/product/hot', { params: { limit } })
}

export const getNewProducts = (limit = 10) => {
  return request.get('/product/new', { params: { limit } })
}

export const getCategoryList = () => {
  return request.get('/product/category/list')
}