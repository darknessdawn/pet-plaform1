package com.pet.service;

import com.pet.vo.CartVO;

import java.util.List;

public interface CartService {
    
    CartVO addToCart(Long userId, Long productId, Integer quantity);
    
    CartVO updateCartItem(Long userId, Long cartId, Integer quantity);
    
    void deleteCartItem(Long userId, Long cartId);
    
    void clearCart(Long userId);
    
    List<CartVO> getCartList(Long userId);
    
    void selectCartItem(Long userId, Long cartId, Integer selected);
    
    void selectAllCartItems(Long userId, Integer selected);
    
    Integer getCartCount(Long userId);
}
