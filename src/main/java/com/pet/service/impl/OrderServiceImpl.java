package com.pet.service.impl;

import com.pet.common.BusinessException;
import com.pet.dto.OrderCreateDTO;
import com.pet.entity.*;
import com.pet.mapper.*;
import com.pet.service.OrderService;
import com.pet.service.RecommendationService;
import com.pet.service.MembershipService;
import com.pet.service.CouponService;
import com.pet.vo.OrderDetailVO;
import com.pet.vo.OrderVO;
import com.pet.vo.UserCouponVO;
import com.pet.vo.UserMembershipVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final RecommendationService recommendationService;
    private final MembershipService membershipService;
    private final CouponService couponService;
    
    @Override
    @Transactional
    public OrderVO createOrder(Long userId, OrderCreateDTO orderDTO) {
        // 获取地址信息
        Address address = addressMapper.selectById(orderDTO.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        
        // 获取购物车选中的商品
        List<Cart> carts;
        if (orderDTO.getCartIds() != null && !orderDTO.getCartIds().isEmpty()) {
            carts = new ArrayList<>();
            for (Long cartId : orderDTO.getCartIds()) {
                Cart cart = cartMapper.selectById(cartId);
                if (cart != null && cart.getUserId().equals(userId) && cart.getSelected() == 1) {
                    carts.add(cart);
                }
            }
        } else {
            carts = cartMapper.selectByUserId(userId).stream()
                    .filter(cart -> cart.getSelected() == 1)
                    .collect(Collectors.toList());
        }
        
        if (carts.isEmpty()) {
            throw new BusinessException("购物车为空或未选择商品");
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(0); // 待付款
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + 
                address.getDistrict() + address.getDetailAddress());
        order.setRemark(orderDTO.getRemark());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();
                
        // 处理每个商品（使用乐观锁扣减库存）
        for (Cart cart : carts) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getDeleted() == 1) {
                throw new BusinessException("商品不存在：" + cart.getProductId());
            }
            if (product.getStatus() != 1) {
                throw new BusinessException("商品已下架：" + product.getName());
            }
                    
            // 使用乐观锁扣减库存
            int affected = productMapper.reduceStock(product.getId(), cart.getQuantity(), product.getVersion());
            if (affected == 0) {
                throw new BusinessException("商品库存不足：" + product.getName());
            }
                    
            // 创建订单详情
            OrderDetail detail = new OrderDetail();
            detail.setProductId(product.getId());
            detail.setProductName(product.getName());
            detail.setProductImage(product.getMainImage());
            detail.setProductPrice(product.getPrice());
            detail.setQuantity(cart.getQuantity());
            detail.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            orderDetails.add(detail);
                    
            totalAmount = totalAmount.add(detail.getTotalAmount());
                    
            // 记录购买行为（用于推荐算法）
            recommendationService.recordUserBehavior(userId, product.getId(), 4, 5.0);
                    
            // 删除购物车项
            cartMapper.deleteById(cart.getId());
        }
                
        // 计算会员折扣
        UserMembershipVO membership = membershipService.getMyMembership(userId);
        BigDecimal memberDiscount = BigDecimal.ONE;
        if (membership != null && membership.getDiscountRate() != null) {
            memberDiscount = membership.getDiscountRate();
        }
        BigDecimal discountedAmount = totalAmount.multiply(memberDiscount);
                
        // 使用优惠券
        BigDecimal couponDiscount = BigDecimal.ZERO;
        Long usedCouponId = null;
        if (orderDTO.getCouponId() != null) {
            // 验证并使用优惠券
            couponDiscount = couponService.calculateDiscount(orderDTO.getCouponId(), discountedAmount);
            if (couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
                // 先创建订单，再使用优惠券（在 order 对象保存后）
                usedCouponId = orderDTO.getCouponId();
            }
        }
                
        // 计算最终应付金额
        BigDecimal payAmount = discountedAmount.subtract(couponDiscount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }
                
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(couponDiscount);
        order.setPayAmount(payAmount);
        order.setMemberDiscount(memberDiscount);
        order.setUserCouponId(usedCouponId);
        orderMapper.insert(order);
        
        // 订单保存后，使用优惠券
        if (usedCouponId != null) {
            couponService.useCoupon(userId, usedCouponId, order.getId());
        }
        
        // 保存订单详情
        for (OrderDetail detail : orderDetails) {
            detail.setOrderId(order.getId());
            orderDetailMapper.insert(detail);
        }
        
        return convertToVO(order);
    }
    
    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        return convertToVO(order);
    }
    
    @Override
    public List<OrderVO> getOrderList(Long userId, Integer status) {
        List<Order> orders;
        if (status != null) {
            orders = orderMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                            .eq(Order::getUserId, userId)
                            .eq(Order::getStatus, status)
                            .eq(Order::getDeleted, 0)
                            .orderByDesc(Order::getCreateTime)
            );
        } else {
            orders = orderMapper.selectByUserId(userId);
        }
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待付款订单");
        }
        
        order.setStatus(5); // 已取消
        orderMapper.updateById(order);
        
        // 恢复库存
        restoreStock(orderId);
    }
    
    @Override
    @Transactional
    public void payOrder(Long userId, Long orderId, Integer payType) {
        log.info("开始支付订单，userId: {}, orderId: {}, payType: {}", userId, orderId, payType);
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在：{}", orderId);
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            log.error("订单不属于当前用户，userId: {}, orderUserId: {}", userId, order.getUserId());
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            log.error("订单状态错误，orderId: {}, status: {}", orderId, order.getStatus());
            throw new BusinessException("订单状态错误");
        }
        
        order.setStatus(1); // 已付款
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单支付成功，orderId: {}", orderId);
        
        // 支付成功后赠送积分（添加异常处理）
        try {
            if (order.getPayAmount() != null) {
                int points = membershipService.calculatePoints(userId, order.getPayAmount().doubleValue());
                if (points > 0) {
                    membershipService.addPoints(userId, points, "ORDER_PAY", orderId, 
                        "订单支付赠送积分，金额：" + order.getPayAmount());
                    log.info("积分赠送成功，userId: {}, points: {}", userId, points);
                }
            }
        } catch (Exception e) {
            // 记录积分赠送失败的错误，但不影响订单支付
            log.error("积分赠送失败：{}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void confirmReceive(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态错误");
        }
        
        order.setStatus(3); // 已收货
        order.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
    
    @Override
    @Transactional
    public void shipOrder(Long userId, Long orderId) {
        log.info("开始发货订单，userId: {}, orderId: {}", userId, orderId);
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在：{}", orderId);
            throw new BusinessException("订单不存在");
        }
        // 管理员可以查看所有订单，所以不检查 userId
        if (order.getStatus() != 1) {
            log.error("订单状态错误，orderId: {}, status: {}", orderId, order.getStatus());
            throw new BusinessException("订单状态错误，只有待发货订单才能发货");
        }
        
        order.setStatus(2); // 已发货
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单发货成功，orderId: {}", orderId);
    }
    
    @Override
    @Transactional
    public void applyRefund(Long userId, Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("当前订单状态不能申请退款");
        }
        
        order.setStatus(6); // 退款中
        orderMapper.updateById(order);
    }
    
    private void restoreStock(Long orderId) {
        List<OrderDetail> details = orderDetailMapper.selectByOrderId(orderId);
        for (OrderDetail detail : details) {
            Product product = productMapper.selectById(detail.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + detail.getQuantity());
                productMapper.updateById(product);
            }
        }
    }
    
    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                (int) (Math.random() * 10000);
    }
    
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusText(getStatusText(order.getStatus()));
        
        // 获取订单详情
        List<OrderDetail> details = orderDetailMapper.selectByOrderId(order.getId());
        List<OrderDetailVO> detailVOs = details.stream().map(detail -> {
            OrderDetailVO detailVO = new OrderDetailVO();
            BeanUtils.copyProperties(detail, detailVO);
            return detailVO;
        }).collect(Collectors.toList());
        vo.setOrderDetails(detailVOs);
        
        return vo;
    }
    
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待付款";
            case 1: return "已付款";
            case 2: return "已发货";
            case 3: return "已收货";
            case 4: return "已完成";
            case 5: return "已取消";
            case 6: return "退款中";
            case 7: return "已退款";
            default: return "未知";
        }
    }
}
