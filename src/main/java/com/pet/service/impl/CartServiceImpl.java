package com.pet.service.impl;

import com.pet.common.BusinessException;
import com.pet.entity.Cart;
import com.pet.entity.Product;
import com.pet.mapper.CartMapper;
import com.pet.mapper.ProductMapper;
import com.pet.service.CartService;
import com.pet.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    
    @Override
    public CartVO addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }
        
        // 检查购物车是否已有该商品
        Cart existingCart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if (existingCart != null) {
            // 更新数量
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            cartMapper.updateById(existingCart);
            return convertToVO(existingCart);
        }
        
        // 新增购物车项
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setSelected(1);
        cartMapper.insert(cart);
        
        return convertToVO(cart);
    }
    
    @Override
    public CartVO updateCartItem(Long userId, Long cartId, Integer quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        
        if (quantity <= 0) {
            cartMapper.deleteById(cartId);
            return null;
        }
        
        // 检查库存
        Product product = productMapper.selectById(cart.getProductId());
        if (product != null && product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }
        
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        
        return convertToVO(cart);
    }
    
    @Override
    public void deleteCartItem(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        cartMapper.deleteById(cartId);
    }
    
    @Override
    public void clearCart(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        for (Cart cart : carts) {
            cartMapper.deleteById(cart.getId());
        }
    }
    
    @Override
    public List<CartVO> getCartList(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        return carts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void selectCartItem(Long userId, Long cartId, Integer selected) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        cart.setSelected(selected);
        cartMapper.updateById(cart);
    }
    
    @Override
    public void selectAllCartItems(Long userId, Integer selected) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        for (Cart cart : carts) {
            cart.setSelected(selected);
            cartMapper.updateById(cart);
        }
    }
    
    @Override
    public Integer getCartCount(Long userId) {
        return cartMapper.countByUserId(userId);
    }
    
    private CartVO convertToVO(Cart cart) {
        CartVO vo = new CartVO();
        BeanUtils.copyProperties(cart, vo);
        
        Product product = productMapper.selectById(cart.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductImage(product.getMainImage());
            vo.setProductPrice(product.getPrice());
            vo.setStock(product.getStock());
            vo.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }
        
        return vo;
    }
}
