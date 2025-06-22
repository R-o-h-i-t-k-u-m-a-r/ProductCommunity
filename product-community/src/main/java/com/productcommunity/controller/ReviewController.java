package com.productcommunity.controller;

import com.productcommunity.dto.UserDTO;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.request.ProductReviewRequest;
import com.productcommunity.response.ApiResponse;
import com.productcommunity.response.ReviewResponse;
import com.productcommunity.service.review.ReviewService;
import com.productcommunity.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/reviews")
@Slf4j
@Validated
public class ReviewController {

    private final ReviewService reviewService;
    private final IUserService userService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ApiResponse> createReview(
            @Valid @RequestBody ProductReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            UserDTO userDTO = userService.getByUserName(username);
            ReviewResponse response = reviewService.createReview(request, userDTO.getId());
            return ResponseEntity.ok(new ApiResponse("review created ", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
        catch ( IllegalStateException e){
            return ResponseEntity.status(NOT_ACCEPTABLE).body(new ApiResponse(e.getMessage(), null));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ProductReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ReviewResponse response = reviewService.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ReviewResponse> responses = reviewService.getApprovedReviewsByProduct(productId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public ResponseEntity<Page<ReviewResponse>> getUserReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        String username = userDetails.getUsername();
        UserDTO userDTO = userService.getByUserName(username);

        Page<ReviewResponse> responses = reviewService.getUserReviews(userDTO.getId(), pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<Map<String, Object>> getProductReviewStats(
            @PathVariable Long productId) {
        Double averageRating = reviewService.getProductAverageRating(productId);
        long reviewCount = reviewService.countApprovedReviewsForProduct(productId);

        return ResponseEntity.ok(Map.of(
                "averageRating", averageRating != null ? averageRating : 0,
                "reviewCount", reviewCount
        ));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    // Admin endpoints
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<ReviewResponse> approveReview(@PathVariable Long reviewId) {
        ReviewResponse response = reviewService.approveReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{reviewId}/reject")
    public ResponseEntity<ReviewResponse> rejectReview(
            @PathVariable Long reviewId,
            @RequestParam(required = false) String reason) {
        ReviewResponse response = reviewService.rejectReview(reviewId, reason);
        return ResponseEntity.ok(response);
    }

}
