package com.pet.service;

import com.pet.dto.ProductDTO;
import com.pet.vo.CategoryVO;
import com.pet.vo.ProductVO;

import java.util.List;
import java.util.Map;

public interface ProductService {
    
    ProductVO createProduct(Long sellerId, ProductDTO productDTO);
    
    ProductVO updateProduct(Long sellerId, ProductDTO productDTO);
    
    void deleteProduct(Long sellerId, Long productId);
    
    ProductVO getProductDetail(Long productId);
    
    List<ProductVO> getProductList(Long categoryId, String keyword, Integer page, Integer size);
    
    Map<String, Object> getProductListWithPage(Long categoryId, String keyword, Integer page, Integer size);
    
    List<ProductVO> getHotProducts(Integer limit);
    
    List<ProductVO> getNewProducts(Integer limit);
    
    List<CategoryVO> getCategoryList();
    
    List<ProductVO> getSellerProducts(Long sellerId);
}
