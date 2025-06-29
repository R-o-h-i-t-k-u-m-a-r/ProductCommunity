package com.productcommunity.service.review;

import com.productcommunity.dto.ReviewRequestDTO;
import com.productcommunity.model.Review;
import com.productcommunity.request.CreateReviewRequest;
import com.productcommunity.request.ProductReviewRequest;
import com.productcommunity.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IReviewService {
    ReviewResponse createReview(ProductReviewRequest request, Long userId);
    ReviewResponse updateReview(Long reviewId, ProductReviewRequest request, Long userId);
    Page<ReviewResponse> getApprovedReviewsByProduct(Long productId, Pageable pageable);
    Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable);
    void deleteReview(Long reviewId, Long userId);
    Double getProductAverageRating(Long productId);
    long countApprovedReviewsForProduct(Long productId);
    ReviewResponse approveReview(Long reviewId);
    ReviewResponse rejectReview(Long reviewId, String reason);
    List<ReviewResponse> allPendingReviews();

    ReviewRequestDTO requestProductReview(CreateReviewRequest request);
    List<ReviewRequestDTO> getAllReviewRequests();

    ReviewRequestDTO approveRequestProductReview(Long id);
    ReviewRequestDTO rejectRequestProductReview(Long id);
    List<ReviewRequestDTO> getAllPendingReviewRequests();
    List<ReviewRequestDTO> getUserAllRequestProductReview();

}
