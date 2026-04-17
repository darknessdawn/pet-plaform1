package com.pet.service;

import com.pet.entity.Product;
import com.pet.vo.ProductVO;

import java.util.List;

public interface RecommendationService {
    
    /**
     * 为用户推荐商品 (混合推荐策略)
     */
    List<ProductVO> recommendForUser(Long userId, int numRecommendations);
    
    /**
     * 基于UserCF推荐
     */
    List<ProductVO> recommendByUserCF(Long userId, int numRecommendations);
    
    /**
     * 基于ItemCF推荐
     */
    List<ProductVO> recommendByItemCF(Long userId, int numRecommendations);
    
    /**
     * 基于SVD矩阵分解推荐
     */
    List<ProductVO> recommendBySVD(Long userId, int numRecommendations);
    
    /**
     * 获取相似商品
     */
    List<ProductVO> getSimilarProducts(Long productId, int numSimilar);
    
    /**
     * 记录用户行为
     */
    void recordUserBehavior(Long userId, Long productId, Integer behaviorType, Double score);
    
    /**
     * 计算用户相似度
     */
    double calculateUserSimilarity(Long userId1, Long userId2);
    
    /**
     * 计算商品相似度
     */
    double calculateItemSimilarity(Long itemId1, Long itemId2);
    
    /**
     * 冷启动推荐 (新用户)
     */
    List<ProductVO> coldStartRecommendation(int numRecommendations);
    
    /**
     * 获取热门推荐
     */
    List<ProductVO> getHotRecommendations(int limit);
    
    /**
     * 获取新品推荐
     */
    List<ProductVO> getNewRecommendations(int limit);
}
