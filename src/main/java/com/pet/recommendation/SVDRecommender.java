package com.pet.recommendation;

import com.pet.config.RecommendationConfig;
import com.pet.entity.Product;
import com.pet.entity.UserBehavior;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.UserBehaviorMapper;
import com.pet.vo.ProductVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.*;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SVD矩阵分解推荐算法
 * 用于降维处理稀疏矩阵，缓解数据稀疏问题
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SVDRecommender {

    private final UserBehaviorMapper userBehaviorMapper;
    private final ProductMapper productMapper;
    private final RecommendationConfig recommendationConfig;

    // SVD分解后的矩阵
    private RealMatrix userFeatures;
    private RealMatrix itemFeatures;

    // 用户ID和商品ID到矩阵索引的映射
    private Map<Long, Integer> userIdToIndex;
    private Map<Long, Integer> itemIdToIndex;
    private Map<Integer, Long> indexToItemId;

    // 原始评分矩阵
    private RealMatrix ratingMatrix;

    // 存储每个用户的平均分（用于去均值后的还原）
    private double[] userMeans;

    /**
     * 初始化SVD模型
     */
    @PostConstruct
    public void init() {
        log.info("初始化SVD推荐模型...");
        trainModel();
    }

    /**
     * 定时重新训练模型 (每天凌晨2点)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledTraining() {
        log.info("定时重新训练SVD模型...");
        trainModel();
    }

    /**
     * 训练SVD模型
     */
    public synchronized void trainModel() {
        try {
            // 获取所有行为数据
            List<UserBehavior> behaviors = userBehaviorMapper.selectList(null);
            if (behaviors.isEmpty()) {
                log.warn("没有用户行为数据，无法训练SVD模型");
                return;
            }

            // 构建ID映射
            buildIdMappings(behaviors);

            // 构建评分矩阵
            buildRatingMatrix(behaviors);

            // 执行SVD分解
            performSVD();

            log.info("SVD模型训练完成，用户数: {}, 商品数: {}",
                    userIdToIndex.size(), itemIdToIndex.size());
        } catch (Exception e) {
            log.error("SVD模型训练失败: ", e);
        }
    }

    /**
     * 为用户推荐商品
     */
    public List<ProductVO> recommend(Long userId, int numRecommendations) {
        if (userFeatures == null || itemFeatures == null) {
            log.warn("SVD模型未初始化，返回热门推荐");
            return getHotProducts(numRecommendations);
        }

        Integer userIndex = userIdToIndex.get(userId);
        if (userIndex == null) {
            log.warn("用户 {} 不在SVD模型中，返回热门推荐", userId);
            return getHotProducts(numRecommendations);
        }

        // 获取用户特征向量
        RealVector userVector = userFeatures.getRowVector(userIndex);
        double userMean = userMeans[userIndex];

        // 计算对所有商品的预测评分
        Map<Long, Double> predictedScores = new HashMap<>();

        for (int i = 0; i < itemFeatures.getRowDimension(); i++) {
            RealVector itemVector = itemFeatures.getRowVector(i);
            double score = userVector.dotProduct(itemVector) + userMean; // 加回用户平均分
            Long itemId = indexToItemId.get(i);
            if (itemId != null) {
                predictedScores.put(itemId, score);
            }
        }

        // 获取用户已交互的商品
        Set<Long> interactedItems = getUserInteractedItems(userId);

        // 过滤已交互商品，返回Top-N
        return predictedScores.entrySet().stream()
                .filter(entry -> !interactedItems.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(numRecommendations)
                .map(entry -> convertToVO(productMapper.selectById(entry.getKey())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 预测用户对商品的评分
     */
    public double predictRating(Long userId, Long itemId) {
        Integer userIndex = userIdToIndex.get(userId);
        Integer itemIndex = itemIdToIndex.get(itemId);

        if (userIndex == null || itemIndex == null) {
            return 0.0;
        }

        RealVector userVector = userFeatures.getRowVector(userIndex);
        RealVector itemVector = itemFeatures.getRowVector(itemIndex);
        double userMean = userMeans[userIndex];

        return userVector.dotProduct(itemVector) + userMean;
    }

    /**
     * 构建ID映射
     */
    private void buildIdMappings(List<UserBehavior> behaviors) {
        userIdToIndex = new HashMap<>();
        itemIdToIndex = new HashMap<>();
        indexToItemId = new HashMap<>();

        int userIndex = 0;
        int itemIndex = 0;

        for (UserBehavior behavior : behaviors) {
            if (!userIdToIndex.containsKey(behavior.getUserId())) {
                userIdToIndex.put(behavior.getUserId(), userIndex++);
            }
            if (!itemIdToIndex.containsKey(behavior.getProductId())) {
                itemIdToIndex.put(behavior.getProductId(), itemIndex);
                indexToItemId.put(itemIndex, behavior.getProductId());
                itemIndex++;
            }
        }
    }

    /**
     * 构建评分矩阵
     */
    private void buildRatingMatrix(List<UserBehavior> behaviors) {
        int numUsers = userIdToIndex.size();
        int numItems = itemIdToIndex.size();

        ratingMatrix = new Array2DRowRealMatrix(numUsers, numItems);
        userMeans = new double[numUsers];

        // 填充评分矩阵
        for (UserBehavior behavior : behaviors) {
            int userIdx = userIdToIndex.get(behavior.getUserId());
            int itemIdx = itemIdToIndex.get(behavior.getProductId());
            double score = behavior.getScore() != null ? behavior.getScore() : 1.0;

            // 如果同一用户同一商品有多个行为，取最高分
            double currentScore = ratingMatrix.getEntry(userIdx, itemIdx);
            ratingMatrix.setEntry(userIdx, itemIdx, Math.max(currentScore, score));
        }

        // 矩阵归一化（减去用户均值）
        normalizeMatrix();
    }

    /**
     * 矩阵归一化
     */
    private void normalizeMatrix() {
        int numUsers = ratingMatrix.getRowDimension();
        int numItems = ratingMatrix.getColumnDimension();

        for (int i = 0; i < numUsers; i++) {
            double[] row = ratingMatrix.getRow(i);
            double sum = 0.0;
            int count = 0;

            for (double value : row) {
                if (value > 0) {
                    sum += value;
                    count++;
                }
            }

            if (count > 0) {
                double mean = sum / count;
                userMeans[i] = mean;
                for (int j = 0; j < numItems; j++) {
                    if (ratingMatrix.getEntry(i, j) > 0) {
                        ratingMatrix.setEntry(i, j, ratingMatrix.getEntry(i, j) - mean);
                    }
                }
            } else {
                userMeans[i] = 0.0;
            }
        }
    }

    /**
     * 执行SVD分解
     */
    private void performSVD() {
        try {
            SingularValueDecomposition svd = new SingularValueDecomposition(ratingMatrix);

            RealMatrix u = svd.getU();
            RealMatrix s = svd.getS();
            RealMatrix v = svd.getV();

            // 降维维度
            int k = Math.min(recommendationConfig.getSvdDimensions(),
                    Math.min(u.getColumnDimension(), v.getColumnDimension()));

            // 截取前k个奇异值和对应的向量
            RealMatrix uReduced = u.getSubMatrix(0, u.getRowDimension() - 1, 0, k - 1);
            RealMatrix sReduced = s.getSubMatrix(0, k - 1, 0, k - 1);
            RealMatrix vReduced = v.getSubMatrix(0, v.getRowDimension() - 1, 0, k - 1);

            // 计算 sqrt(Sk)
            RealMatrix sqrtS = new Array2DRowRealMatrix(k, k);
            for (int i = 0; i < k; i++) {
                sqrtS.setEntry(i, i, Math.sqrt(sReduced.getEntry(i, i)));
            }

            // 用户特征矩阵 = Uk * sqrt(Sk)
            userFeatures = uReduced.multiply(sqrtS);

            // 物品特征矩阵 = Vk * sqrt(Sk)
            itemFeatures = vReduced.multiply(sqrtS);

        } catch (Exception e) {
            log.error("SVD分解失败: ", e);
            throw e;
        }
    }

    /**
     * 获取用户已交互的商品
     */
    private Set<Long> getUserInteractedItems(Long userId) {
        List<UserBehavior> behaviors = userBehaviorMapper.selectByUserId(userId);
        return behaviors.stream()
                .map(UserBehavior::getProductId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取热门商品
     */
    private List<ProductVO> getHotProducts(int limit) {
        return productMapper.selectHotProducts(limit).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private ProductVO convertToVO(Product product) {
        if (product == null) return null;
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }
}