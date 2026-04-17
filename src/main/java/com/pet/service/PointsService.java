package com.pet.service;

import com.pet.vo.PointsProductVO;

import java.util.List;

public interface PointsService {

    /**
     * 获取积分商城商品列表
     */
    List<PointsProductVO> getPointsProducts();

    /**
     * 兑换积分商品
     */
    boolean exchangePointsProduct(Long userId, Long productId);

    /**
     * 我的积分订单
     */
    List<Object> getMyPointsOrders(Long userId);
}
