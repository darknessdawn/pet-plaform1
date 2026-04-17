package com.pet.service;

import com.pet.dto.ReviewDTO;
import com.pet.vo.ReviewVO;

import java.util.List;

public interface ReviewService {
    ReviewVO addReview(Long userId, ReviewDTO reviewDTO);
    ReviewVO updateReview(Long userId, Long reviewId, ReviewDTO reviewDTO);
    void deleteReview(Long userId, Long reviewId);
    ReviewVO getReviewById(Long reviewId);
    List<ReviewVO> getReviewsByProductId(Long productId);
    List<ReviewVO> getReviewsByUserId(Long userId);
}