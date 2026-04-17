package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.CartService;
import com.pet.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车管理", description = "购物车相关接口")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public Result<CartVO> addToCart(@RequestParam Long productId,
                                    @RequestParam(defaultValue = "1") Integer quantity,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CartVO cartVO = cartService.addToCart(userId, productId, quantity);
        return Result.success(cartVO);
    }
    
    @Operation(summary = "获取购物车列表")
    @GetMapping("/list")
    public Result<List<CartVO>> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CartVO> list = cartService.getCartList(userId);
        return Result.success(list);
    }
    
    @Operation(summary = "更新购物车商品数量")
    @PutMapping("/update/{cartId}")
    public Result<CartVO> updateCartItem(@PathVariable Long cartId,
                                         @RequestParam Integer quantity,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CartVO cartVO = cartService.updateCartItem(userId, cartId, quantity);
        return Result.success(cartVO);
    }
    
    @Operation(summary = "删除购物车商品")
    @DeleteMapping("/delete/{cartId}")
    public Result<Void> deleteCartItem(@PathVariable Long cartId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.deleteCartItem(userId, cartId);
        return Result.success();
    }
    
    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        return Result.success();
    }
    
    @Operation(summary = "选择/取消选择购物车商品")
    @PutMapping("/select/{cartId}")
    public Result<Void> selectCartItem(@PathVariable Long cartId,
                                       @RequestParam Integer selected,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.selectCartItem(userId, cartId, selected);
        return Result.success();
    }
    
    @Operation(summary = "全选/取消全选")
    @PutMapping("/select-all")
    public Result<Void> selectAllCartItems(@RequestParam Integer selected,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.selectAllCartItems(userId, selected);
        return Result.success();
    }
    
    @Operation(summary = "获取购物车商品数量")
    @GetMapping("/count")
    public Result<Integer> getCartCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer count = cartService.getCartCount(userId);
        return Result.success(count);
    }
}
