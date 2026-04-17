package com.pet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.Result;
import com.pet.dto.ProductDTO;
import com.pet.service.ProductService;
import com.pet.vo.CategoryVO;
import com.pet.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "商品管理", description = "商品查询、发布、管理等接口")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @Operation(summary = "获取商品列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = productService.getProductListWithPage(categoryId, keyword, page, size);
        return Result.success(result);
    }
    
    @Operation(summary = "获取商品详情")
    @GetMapping("/detail/{productId}")
    public Result<ProductVO> getProductDetail(@PathVariable Long productId) {
        ProductVO product = productService.getProductDetail(productId);
        return Result.success(product);
    }
    
    @Operation(summary = "获取热门商品")
    @GetMapping("/hot")
    public Result<List<ProductVO>> getHotProducts(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<ProductVO> list = productService.getHotProducts(limit);
        return Result.success(list);
    }
    
    @Operation(summary = "获取新品")
    @GetMapping("/new")
    public Result<List<ProductVO>> getNewProducts(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<ProductVO> list = productService.getNewProducts(limit);
        return Result.success(list);
    }
    
    @Operation(summary = "获取分类列表")
    @GetMapping("/category/list")
    public Result<List<CategoryVO>> getCategoryList() {
        List<CategoryVO> list = productService.getCategoryList();
        return Result.success(list);
    }
    
    @Operation(summary = "发布商品")
    @PostMapping
    public Result<ProductVO> createProduct(@Valid @RequestBody ProductDTO productDTO, 
                                           HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        ProductVO product = productService.createProduct(sellerId, productDTO);
        return Result.success(product);
    }
    
    @Operation(summary = "更新商品")
    @PutMapping
    public Result<ProductVO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                           HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        ProductVO product = productService.updateProduct(sellerId, productDTO);
        return Result.success(product);
    }
    
    @Operation(summary = "删除商品")
    @DeleteMapping("/{productId}")
    public Result<Void> deleteProduct(@PathVariable Long productId, HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        productService.deleteProduct(sellerId, productId);
        return Result.success();
    }
    
    @Operation(summary = "获取我的商品列表")
    @GetMapping("/my")
    public Result<List<ProductVO>> getMyProducts(HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        List<ProductVO> list = productService.getSellerProducts(sellerId);
        return Result.success(list);
    }
}
