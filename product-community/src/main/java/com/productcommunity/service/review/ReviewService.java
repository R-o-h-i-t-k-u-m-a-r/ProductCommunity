package com.productcommunity.service.review;

import com.productcommunity.enums.ReviewStatus;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.Product;
import com.productcommunity.model.Review;
import com.productcommunity.model.ReviewRequest;
import com.productcommunity.model.User;
import com.productcommunity.repository.ProductRepository;
import com.productcommunity.repository.ReviewRepository;
import com.productcommunity.repository.UserRepository;
import com.productcommunity.request.ProductReviewRequest;
import com.productcommunity.response.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(ProductReviewRequest request, Long userId) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user already reviewed this product
        if (reviewRepository.findByProductAndUser(product.getId(), user.getId()).isPresent()) {
            throw new IllegalStateException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setStatus(ReviewStatus.PENDING);

        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ProductReviewRequest request, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("You can only edit your own reviews");
        }

        if (review.getStatus() == ReviewStatus.APPROVED) {
            review.setStatus(ReviewStatus.PENDING); // Needs re-approval after edit
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());

        Review updatedReview = reviewRepository.save(review);
        return mapToReviewResponse(updatedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getApprovedReviewsByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable)
                .map(this::mapToReviewResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(this::mapToReviewResponse);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public Double getProductAverageRating(Long productId) {
        return reviewRepository.calculateAverageRatingByProductId(productId);
    }

    @Transactional(readOnly = true)
    public long countApprovedReviewsForProduct(Long productId) {
        return reviewRepository.countByProductIdAndStatus(productId, ReviewStatus.APPROVED);
    }

    // Admin-only methods
    @Transactional
    public ReviewResponse approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setStatus(ReviewStatus.APPROVED);
        return mapToReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse rejectReview(Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setStatus(ReviewStatus.REJECTED);
        // You might want to store the rejection reason in another field
        return mapToReviewResponse(reviewRepository.save(review));
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setProductId(review.getProduct().getId());
        response.setProductName(review.getProduct().getName());
        response.setUserId(review.getUser().getId());
        response.setUserName(review.getUser().getUserName());
        response.setRating(review.getRating());
        response.setTitle(review.getTitle());
        response.setContent(review.getContent());
        response.setStatus(review.getStatus());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}
