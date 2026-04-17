import request from '../utils/request'

export const getPersonalRecommendations = (limit = 10) => {
  return request.get('/recommend/personal', { params: { limit } })
}

export const getHotRecommendations = (limit = 10) => {
  return request.get('/recommend/hot', { params: { limit } })
}

export const getSimilarProducts = (productId, limit = 5) => {
  return request.get(`/recommend/similar/${productId}`, { params: { limit } })
}

export const recordBehavior = (productId, behaviorType, score) => {
  return request.post('/recommend/behavior', null, {
    params: { productId, behaviorType, score }
  })
}
