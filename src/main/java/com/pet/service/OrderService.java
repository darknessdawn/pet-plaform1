package com.pet.service;

import com.pet.dto.OrderCreateDTO;
import com.pet.vo.OrderVO;

import java.util.List;

public interface OrderService {
    
    OrderVO createOrder(Long userId, OrderCreateDTO orderDTO);
    
    OrderVO getOrderDetail(Long userId, Long orderId);
    
    List<OrderVO> getOrderList(Long userId, Integer status);
    
    void cancelOrder(Long userId, Long orderId);
    
    void payOrder(Long userId, Long orderId, Integer payType);
    
    void confirmReceive(Long userId, Long orderId);
    
    void applyRefund(Long userId, Long orderId, String reason);
    
    void shipOrder(Long userId, Long orderId);
}
