package com.pet.service.impl;

import com.pet.config.RecommendationConfig;
import com.pet.entity.Product;
import com.pet.entity.User;
import com.pet.entity.UserBehavior;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.UserBehaviorMapper;
import com.pet.mapper.UserMapper;
import com.pet.recommendation.SVDRecommender;
import com.pet.service.RecommendationService;
import com.pet.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserBehaviorMapper userBehaviorMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final RecommendationConfig recommendationConfig;
    private final SVDRecommender svdRecommender;

    // 行为类型权重（大幅提高购买权重）
    private static final Map<Integer, Double> BEHAVIOR_WEIGHTS = new HashMap<>();
    static {
        BEHAVIOR_WEIGHTS.put(1, 1.0);   // 浏览
        BEHAVIOR_WEIGHTS.put(2, 3.0);   // 收藏
        BEHAVIOR_WEIGHTS.put(3, 4.0);   // 加购
        BEHAVIOR_WEIGHTS.put(4, 50.0);  // 购买（权重调高至50）
        BEHAVIOR_WEIGHTS.put(5, 4.0);   // 评价
    }

    // 时间衰减半衰期（7天）
    private static final double TIME_DECAY_HALF_LIFE_DAYS = 7.0;
    private static final double TIME_DECAY_LAMBDA = Math.log(2) / TIME_DECAY_HALF_LIFE_DAYS;

    // 缓存
    private final Map<Long, List<ProductVO>> recommendationCache = new ConcurrentHashMap<>();
    private final Map<Long, Long> cacheTimestamp = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 10 * 1000; // 10秒

    @Override
    public List<ProductVO> recommendForUser(Long userId, int numRecommendations) {
        log.info("recommendForUser 被调用, userId={}", userId);
        System.out.println("【调试】recommendForUser 被调用, userId=" + userId);

        if (recommendationCache.containsKey(userId)) {
            Long ts = cacheTimestamp.get(userId);
            if (ts != null && System.currentTimeMillis() - ts < CACHE_TTL) {
                log.info("命中缓存，返回缓存结果");
                System.out.println("【调试】命中缓存");
                return recommendationCache.get(userId).stream().limit(numRecommendations).collect(Collectors.toList());
            } else {
                log.info("缓存已过期，重新计算");
                System.out.println("【调试】缓存已过期");
            }
        } else {
            log.info("无缓存，重新计算");
            System.out.println("【调试】无缓存");
        }

        List<ProductVO> recommendations = computeRecommendations(userId, numRecommendations);
        recommendationCache.put(userId, recommendations);
        cacheTimestamp.put(userId, System.currentTimeMillis());
        log.info("推荐计算完成，结果数量：{}", recommendations.size());
        System.out.println("【调试】推荐计算完成，结果数量：" + recommendations.size());
        return recommendations;
    }

    private List<ProductVO> computeRecommendations(Long userId, int numRecommendations) {
        System.out.println("【调试】开始 computeRecommendations, userId=" + userId);

        // 获取用户所有行为
        List<UserBehavior> userBehaviors = userBehaviorMapper.selectByUserId(userId);

        // 获取用户最近购买的5个商品（按时间倒序）
        List<Long> recentPurchaseIds = userBehaviors.stream()
                .filter(b -> b.getBehaviorType() == 4) // 购买行为
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .map(UserBehavior::getProductId)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
        System.out.println("【调试】最近购买的商品ID：" + recentPurchaseIds);

        // 冷启动：如果用户无行为，返回热门商品
        if (userBehaviors.isEmpty()) {
            log.info("用户 {} 无行为，返回热门商品", userId);
            return getHotRecommendations(numRecommendations);
        }

        // 获取协同过滤推荐结果
        List<Product> cfProducts = getCFRecommendations(userId);
        System.out.println("【调试】协同过滤推荐结果数量：" + cfProducts.size());

        // 构建结果列表，优先加入最近购买的商品
        Set<Long> selectedIds = new HashSet<>();
        List<ProductVO> result = new ArrayList<>();

        // 1. 优先加入最近购买的商品
        for (Long pid : recentPurchaseIds) {
            Product p = productMapper.selectById(pid);
            if (p != null && !selectedIds.contains(pid)) {
                result.add(convertToVO(p));
                selectedIds.add(pid);
                System.out.println("【调试】优先加入最近购买商品 ID=" + pid);
                if (result.size() >= numRecommendations) break;
            }
        }

        // 2. 如果还没满，加入协同过滤推荐的商品（排除已选的）
        if (result.size() < numRecommendations) {
            for (Product p : cfProducts) {
                if (!selectedIds.contains(p.getId())) {
                    result.add(convertToVO(p));
                    selectedIds.add(p.getId());
                    if (result.size() >= numRecommendations) break;
                }
            }
        }

        // 3. 如果还是不足，补充热门商品
        if (result.size() < numRecommendations) {
            List<Product> hot = getHotProducts(numRecommendations * 2);
            for (Product p : hot) {
                if (!selectedIds.contains(p.getId())) {
                    result.add(convertToVO(p));
                    selectedIds.add(p.getId());
                    if (result.size() >= numRecommendations) break;
                }
            }
        }

        System.out.println("【调试】最终推荐商品ID列表：" + result.stream().map(ProductVO::getId).collect(Collectors.toList()));
        return result;
    }

    // ==================== 协同过滤核心方法 ====================

    private List<Product> getCFRecommendations(Long userId) {
        List<UserBehavior> allBehaviors = userBehaviorMapper.selectList(null);
        if (allBehaviors.isEmpty()) return Collections.emptyList();

        // 构建用户-物品评分矩阵
        Map<Long, Map<Long, Double>> userItemMatrix = new HashMap<>();
        Map<Long, Map<Long, Double>> itemUserMatrix = new HashMap<>();

        for (UserBehavior b : allBehaviors) {
            double score = BEHAVIOR_WEIGHTS.getOrDefault(b.getBehaviorType(), 1.0);
            userItemMatrix.computeIfAbsent(b.getUserId(), k -> new HashMap<>())
                    .merge(b.getProductId(), score, Double::sum);
            itemUserMatrix.computeIfAbsent(b.getProductId(), k -> new HashMap<>())
                    .merge(b.getUserId(), score, Double::sum);
        }

        // 计算物品相似度
        Map<Long, Map<Long, Double>> itemSimilarity = calculateItemSimilarity(itemUserMatrix);

        Map<Long, Double> userInteractions = userItemMatrix.getOrDefault(userId, Collections.emptyMap());
        Map<Long, Double> recommendations = new HashMap<>();

        // ItemCF
        for (Map.Entry<Long, Double> entry : userInteractions.entrySet()) {
            Long itemId = entry.getKey();
            Double rating = entry.getValue();
            Map<Long, Double> similar = itemSimilarity.getOrDefault(itemId, Collections.emptyMap());
            for (Map.Entry<Long, Double> simEntry : similar.entrySet()) {
                Long simItem = simEntry.getKey();
                Double sim = simEntry.getValue();
                if (!userInteractions.containsKey(simItem)) {
                    recommendations.merge(simItem, sim * rating * 0.5, Double::sum);
                }
            }
        }

        // UserCF
        Map<Long, Double> userSimilarity = calculateUserSimilarity(userItemMatrix, userId);
        List<Long> similarUsers = userSimilarity.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (Long su : similarUsers) {
            Double sim = userSimilarity.get(su);
            Map<Long, Double> suItems = userItemMatrix.get(su);
            for (Map.Entry<Long, Double> itemEntry : suItems.entrySet()) {
                Long itemId = itemEntry.getKey();
                Double rating = itemEntry.getValue();
                if (!userInteractions.containsKey(itemId)) {
                    recommendations.merge(itemId, sim * rating * 0.5, Double::sum);
                }
            }
        }

        // 排序取前20个
        return recommendations.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(20)
                .map(entry -> productMapper.selectById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Map<Long, Double> calculateUserSimilarity(Map<Long, Map<Long, Double>> matrix, Long targetId) {
        Map<Long, Double> simMap = new HashMap<>();
        Map<Long, Double> target = matrix.get(targetId);
        if (target == null) return simMap;

        double targetNorm = 0.0;
        for (Double v : target.values()) targetNorm += v * v;
        targetNorm = Math.sqrt(targetNorm);

        for (Map.Entry<Long, Map<Long, Double>> entry : matrix.entrySet()) {
            Long otherId = entry.getKey();
            if (otherId.equals(targetId)) continue;
            Map<Long, Double> other = entry.getValue();
            Set<Long> common = new HashSet<>(target.keySet());
            common.retainAll(other.keySet());
            if (common.isEmpty()) continue;

            double dot = 0.0;
            for (Long item : common) dot += target.get(item) * other.get(item);
            double otherNorm = 0.0;
            for (Double v : other.values()) otherNorm += v * v;
            otherNorm = Math.sqrt(otherNorm);
            if (targetNorm > 0 && otherNorm > 0) {
                simMap.put(otherId, dot / (targetNorm * otherNorm));
            }
        }
        return simMap;
    }

    private Map<Long, Map<Long, Double>> calculateItemSimilarity(Map<Long, Map<Long, Double>> itemUserMatrix) {
        Map<Long, Map<Long, Double>> simMatrix = new HashMap<>();
        List<Long> itemIds = new ArrayList<>(itemUserMatrix.keySet());
        Map<Long, Double> itemNorms = new HashMap<>();
        for (Long id : itemIds) {
            double norm = 0.0;
            for (Double v : itemUserMatrix.get(id).values()) norm += v * v;
            itemNorms.put(id, Math.sqrt(norm));
        }

        for (int i = 0; i < itemIds.size(); i++) {
            for (int j = i + 1; j < itemIds.size(); j++) {
                Long a = itemIds.get(i);
                Long b = itemIds.get(j);
                Map<Long, Double> usersA = itemUserMatrix.get(a);
                Map<Long, Double> usersB = itemUserMatrix.get(b);
                Set<Long> common = new HashSet<>(usersA.keySet());
                common.retainAll(usersB.keySet());
                if (common.isEmpty()) continue;

                double dot = 0.0;
                for (Long u : common) dot += usersA.get(u) * usersB.get(u);
                double normA = itemNorms.get(a);
                double normB = itemNorms.get(b);
                if (normA > 0 && normB > 0) {
                    double sim = dot / (normA * normB);
                    simMatrix.computeIfAbsent(a, k -> new HashMap<>()).put(b, sim);
                    simMatrix.computeIfAbsent(b, k -> new HashMap<>()).put(a, sim);
                }
            }
        }
        return simMatrix;
    }

    private List<Product> getHotProducts(int limit) {
        return productMapper.selectHotProducts(limit);
    }

    private ProductVO convertToVO(Product product) {
        if (product == null) return null;
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    // ==================== 接口方法实现 ====================

    @Override
    public List<ProductVO> recommendByUserCF(Long userId, int numRecommendations) {
        return getCFRecommendations(userId).stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> recommendByItemCF(Long userId, int numRecommendations) {
        return recommendByUserCF(userId, numRecommendations);
    }

    @Override
    public List<ProductVO> recommendBySVD(Long userId, int numRecommendations) {
        return svdRecommender.recommend(userId, numRecommendations);
    }

    @Override
    public List<ProductVO> getSimilarProducts(Long productId, int numSimilar) {
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(null);
        if (behaviors.isEmpty()) return Collections.emptyList();

        Map<Long, Map<Long, Double>> itemUserMatrix = new HashMap<>();
        for (UserBehavior b : behaviors) {
            double score = BEHAVIOR_WEIGHTS.getOrDefault(b.getBehaviorType(), 1.0);
            itemUserMatrix.computeIfAbsent(b.getProductId(), k -> new HashMap<>())
                    .merge(b.getUserId(), score, Double::sum);
        }
        Map<Long, Map<Long, Double>> simMatrix = calculateItemSimilarity(itemUserMatrix);
        Map<Long, Double> sims = simMatrix.getOrDefault(productId, Collections.emptyMap());

        return sims.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(numSimilar)
                .map(entry -> productMapper.selectById(entry.getKey()))
                .filter(Objects::nonNull)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void recordUserBehavior(Long userId, Long productId, Integer behaviorType, Double score) {
        System.out.println("========== recordUserBehavior 开始执行 ==========");
        System.out.println("参数: userId=" + userId + ", productId=" + productId + ", type=" + behaviorType + ", score=" + score);
        try {
            UserBehavior existing = userBehaviorMapper.selectByUserProductAndType(userId, productId, behaviorType);
            double finalScore = score != null ? score : BEHAVIOR_WEIGHTS.getOrDefault(behaviorType, 1.0);
            if (existing != null) {
                if (finalScore > existing.getScore()) {
                    existing.setScore(finalScore);
                    userBehaviorMapper.updateById(existing);
                    System.out.println("更新行为 ID=" + existing.getId());
                }
            } else {
                UserBehavior behavior = new UserBehavior();
                behavior.setUserId(userId);
                behavior.setProductId(productId);
                behavior.setBehaviorType(behaviorType);
                behavior.setScore(finalScore);
                userBehaviorMapper.insert(behavior);
                System.out.println("新增行为 ID=" + behavior.getId());
            }
            clearUserCache(userId);
            System.out.println("已清除用户 " + userId + " 的缓存");
        } catch (Exception e) {
            System.out.println("========== 记录行为异常 ==========");
            e.printStackTrace();
        }
        System.out.println("========== recordUserBehavior 结束 ==========");
    }

    public void clearUserCache(Long userId) {
        recommendationCache.remove(userId);
        cacheTimestamp.remove(userId);
    }

    @Override
    public double calculateUserSimilarity(Long userId1, Long userId2) {
        return 0;
    }

    @Override
    public double calculateItemSimilarity(Long itemId1, Long itemId2) {
        return 0;
    }

    @Override
    public List<ProductVO> coldStartRecommendation(int numRecommendations) {
        return getHotRecommendations(numRecommendations);
    }

    @Override
    public List<ProductVO> getHotRecommendations(int limit) {
        return productMapper.selectHotProducts(limit).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getNewRecommendations(int limit) {
        return productMapper.selectNewProducts(limit).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 定时清理过期的缓存条目（每小时执行一次）
     */
    @Scheduled(fixedDelay = 3600000)
    public void cleanCache() {
        long now = System.currentTimeMillis();
        cacheTimestamp.entrySet().removeIf(e -> now - e.getValue() > CACHE_TTL);
        recommendationCache.keySet().retainAll(cacheTimestamp.keySet());
    }
}