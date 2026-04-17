package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.common.BusinessException;
import com.pet.entity.PointsProduct;
import com.pet.entity.PointsOrder;
import com.pet.entity.UserMembership;
import com.pet.mapper.PointsProductMapper;
import com.pet.mapper.PointsOrderMapper;
import com.pet.mapper.UserMembershipMapper;
import com.pet.service.PointsService;
import com.pet.vo.PointsProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final PointsProductMapper pointsProductMapper;
    private final PointsOrderMapper pointsOrderMapper;
    private final UserMembershipMapper userMembershipMapper;

    @Override
    public List<PointsProductVO> getPointsProducts() {
        List<PointsProduct> products = pointsProductMapper.selectList(
            new LambdaQueryWrapper<PointsProduct>()
                .eq(PointsProduct::getStatus, 1)
                .eq(PointsProduct::getDeleted, 0)
                .gt(PointsProduct::getStock, 0)
                .orderByAsc(PointsProduct::getPointsPrice)
        );
        
        return products.stream().map(product -> {
            PointsProductVO vo = new PointsProductVO();
            BeanUtils.copyProperties(product, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exchangePointsProduct(Long userId, Long productId) {
        // 查询商品
        PointsProduct product = pointsProductMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        
        // 检查库存
        if (product.getStock() <= 0) {
            throw new BusinessException("商品库存不足");
        }
        
        // 检查用户积分
        UserMembership membership = userMembershipMapper.selectOne(
            new LambdaQueryWrapper<UserMembership>()
                .eq(UserMembership::getUserId, userId)
        );
        
        if (membership == null || membership.getPoints() < product.getPointsPrice()) {
            throw new BusinessException("积分不足");
        }
        
        // 扣减积分
        membership.setPoints(membership.getPoints() - product.getPointsPrice());
        membership.setUsedPoints(membership.getUsedPoints() + product.getPointsPrice());
        userMembershipMapper.updateById(membership);
        
        // 扣减库存
        product.setStock(product.getStock() - 1);
        pointsProductMapper.updateById(product);
        
        // 创建积分订单
        PointsOrder order = new PointsOrder();
        order.setOrderNo(generateOrderId());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setPointsUsed(product.getPointsPrice());
        order.setCashPaid(product.getCashPrice());
        order.setStatus(1); // 已完成
        pointsOrderMapper.insert(order);
        
        return true;
    }

    @Override
    public List<Object> getMyPointsOrders(Long userId) {
        List<PointsOrder> orders = pointsOrderMapper.selectList(
            new LambdaQueryWrapper<PointsOrder>()
                .eq(PointsOrder::getUserId, userId)
                .orderByDesc(PointsOrder::getCreateTime)
        );
        
        return orders.stream().map(order -> {
            PointsProductVO productVO = new PointsProductVO();
            BeanUtils.copyProperties(order, productVO);
            productVO.setId(order.getProductId());
            return productVO;
        }).collect(Collectors.toList());
    }

    private String generateOrderId() {
        return "P" + System.currentTimeMillis() + (int)(Math.random() * 10000);
    }
}
