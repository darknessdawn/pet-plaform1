package com.pet.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.BusinessException;
import com.pet.dto.ProductDTO;
import com.pet.entity.Category;
import com.pet.entity.Product;
import com.pet.entity.User;
import com.pet.mapper.CategoryMapper;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.UserMapper;
import com.pet.service.ProductService;
import com.pet.vo.CategoryVO;
import com.pet.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    
    @Override
    public ProductVO createProduct(Long sellerId, ProductDTO productDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        product.setSellerId(sellerId);
        product.setSales(0);
        product.setStatus(1);
        product.setReviewCount(0);
        product.setRating(5.0);
        
        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            product.setImages(JSON.toJSONString(productDTO.getImages()));
            product.setMainImage(productDTO.getImages().get(0));
        }
        
        productMapper.insert(product);
        return convertToVO(product);
    }
    
    @Override
    public ProductVO updateProduct(Long sellerId, ProductDTO productDTO) {
        Product product = productMapper.selectById(productDTO.getId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }
        
        BeanUtils.copyProperties(productDTO, product);
        
        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            product.setImages(JSON.toJSONString(productDTO.getImages()));
            product.setMainImage(productDTO.getImages().get(0));
        }
        
        productMapper.updateById(product);
        return convertToVO(product);
    }
    
    @Override
    public void deleteProduct(Long sellerId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此商品");
        }
        
        productMapper.deleteById(productId);
    }
    
    @Override
    public ProductVO getProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在");
        }
        return convertToVO(product);
    }
    
    @Override
    public List<ProductVO> getProductList(Long categoryId, String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getDeleted, 0);
        
        // 处理分类筛选：如果是一级分类，需要包含其所有子分类的商品
        if (categoryId != null) {
            List<Long> categoryIds = getAllSubCategoryIds(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        
        wrapper.orderByDesc(Product::getSales);
        
        Page<Product> pageParam = new Page<>(page, size);
        Page<Product> productPage = productMapper.selectPage(pageParam, wrapper);
        
        return productPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getProductListWithPage(Long categoryId, String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getDeleted, 0);
        
        // 处理分类筛选：如果是一级分类，需要包含其所有子分类的商品
        if (categoryId != null) {
            List<Long> categoryIds = getAllSubCategoryIds(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        
        wrapper.orderByDesc(Product::getSales);
        
        Page<Product> pageParam = new Page<>(page, size);
        Page<Product> productPage = productMapper.selectPage(pageParam, wrapper);
        
        List<ProductVO> list = productPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", productPage.getTotal());
        
        return result;
    }
    
    @Override
    public List<ProductVO> getHotProducts(Integer limit) {
        List<Product> products = productMapper.selectHotProducts(limit);
        return products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductVO> getNewProducts(Integer limit) {
        List<Product> products = productMapper.selectNewProducts(limit);
        return products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryVO> getCategoryList() {
        List<Category> allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .eq(Category::getDeleted, 0)
                        .orderByAsc(Category::getSort)
        );
        
        return buildCategoryTree(allCategories, 0L);
    }
    
    @Override
    public List<ProductVO> getSellerProducts(Long sellerId) {
        List<Product> products = productMapper.selectBySellerId(sellerId);
        return products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    private List<CategoryVO> buildCategoryTree(List<Category> categories, Long parentId) {
        List<CategoryVO> result = new ArrayList<>();
        
        for (Category category : categories) {
            if (category.getParentId().equals(parentId)) {
                CategoryVO vo = new CategoryVO();
                BeanUtils.copyProperties(category, vo);
                vo.setChildren(buildCategoryTree(categories, category.getId()));
                result.add(vo);
            }
        }
        
        return result;
    }
    
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        
        // 解析图片列表
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            try {
                vo.setImages(JSON.parseArray(product.getImages(), String.class));
            } catch (Exception e) {
                vo.setImages(new ArrayList<>());
            }
        }
        
        // 获取分类名称
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        
        // 获取卖家名称
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerName(seller.getNickname() != null ? seller.getNickname() : seller.getUsername());
        }
        
        return vo;
    }
    
    /**
     * 获取指定分类及其所有子分类的 ID 列表
     */
    private List<Long> getAllSubCategoryIds(Long categoryId) {
        List<Long> result = new ArrayList<>();
        result.add(categoryId); // 添加当前分类
        
        // 查询所有子分类
        List<Category> subCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId)
                .eq(Category::getDeleted, 0)
        );
        
        for (Category subCategory : subCategories) {
            result.add(subCategory.getId());
            // 递归查询更深层级的子分类
            result.addAll(getAllSubCategoryIdsRecursive(subCategory.getId()));
        }
        
        return result;
    }
    
    /**
     * 递归获取子分类 ID
     */
    private List<Long> getAllSubCategoryIdsRecursive(Long categoryId) {
        List<Long> result = new ArrayList<>();
        
        List<Category> subCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId)
                .eq(Category::getDeleted, 0)
        );
        
        for (Category subCategory : subCategories) {
            result.add(subCategory.getId());
            result.addAll(getAllSubCategoryIdsRecursive(subCategory.getId()));
        }
        
        return result;
    }
}
