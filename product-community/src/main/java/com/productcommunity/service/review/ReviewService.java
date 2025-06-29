package com.productcommunity.service.review;

import com.productcommunity.dto.ReviewRequestDTO;
import com.productcommunity.dto.UserDTO;
import com.productcommunity.enums.ReviewRequestStatus;
import com.productcommunity.enums.ReviewStatus;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.Product;
import com.productcommunity.model.Review;
import com.productcommunity.model.ReviewRequest;
import com.productcommunity.model.User;
import com.productcommunity.repository.ProductRepository;
import com.productcommunity.repository.ReviewRepository;
import com.productcommunity.repository.ReviewRequestRepository;
import com.productcommunity.repository.UserRepository;
import com.productcommunity.request.CreateReviewRequest;
import com.productcommunity.request.ProductReviewRequest;
import com.productcommunity.response.ReviewResponse;
import com.productcommunity.service.user.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService implements IReviewService{

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private final ReviewRequestRepository reviewRequestRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
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
    @Override
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
    @Override
    public Page<ReviewResponse> getApprovedReviewsByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable)
                .map(this::mapToReviewResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(this::mapToReviewResponse);
    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    @Override
    public Double getProductAverageRating(Long productId) {
        return reviewRepository.calculateAverageRatingByProductId(productId);
    }

    @Transactional(readOnly = true)
    @Override
    public long countApprovedReviewsForProduct(Long productId) {
        return reviewRepository.countByProductIdAndStatus(productId, ReviewStatus.APPROVED);
    }


    @Transactional
    @Override
    public ReviewResponse approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setStatus(ReviewStatus.APPROVED);
        return mapToReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    @Override
    public ReviewResponse rejectReview(Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setStatus(ReviewStatus.REJECTED);
        return mapToReviewResponse(reviewRepository.save(review));
    }

    @Override
    public List<ReviewResponse> allPendingReviews(){
        List<Review> reviews = reviewRepository.findAll();

        List<Review> list = reviews.stream().filter(review -> review.getStatus().equals(ReviewStatus.PENDING)).toList();

        return list.stream().map(this::mapToReviewResponse).toList();
    }

    public ReviewResponse mapToReviewResponse(Review review) {
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

    @Override
    @Transactional
    public ReviewRequestDTO requestProductReview(CreateReviewRequest request) {

        if (productRepository.existsByCode(request.getProductCode())) {
            throw new AlreadyExistsException("Product with code " + request.getProductCode() + " already exists");
        }


        User user = userService.getAuthenticatedUser();


        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setUser(user);
        reviewRequest.setProductName(request.getProductName());
        reviewRequest.setProductCode(request.getProductCode());
        reviewRequest.setProductBrand(request.getProductBrand());
        reviewRequest.setStatus(ReviewRequestStatus.PENDING);

        ReviewRequest savedRequest = reviewRequestRepository.save(reviewRequest);

        return convertToReviewRequestDTO(savedRequest);
    }

    public ReviewRequestDTO convertToReviewRequestDTO(ReviewRequest reviewRequest) {
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setId(reviewRequest.getId());
        dto.setProductName(reviewRequest.getProductName());
        dto.setProductCode(reviewRequest.getProductCode());
        dto.setProductBrand(reviewRequest.getProductBrand());
        dto.setStatus(reviewRequest.getStatus());
        dto.setCreatedAt(reviewRequest.getCreatedAt());


        UserDTO userDTO = modelMapper.map(reviewRequest.getUser(), UserDTO.class);
        dto.setUserDTO(userDTO);

        return dto;
    }

    @Override
    public List<ReviewRequestDTO> getAllReviewRequests(){
        List<ReviewRequest> reviewRequests = reviewRequestRepository.findAll();

        return reviewRequests.stream().map(this::convertToReviewRequestDTO).toList();
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ReviewRequestDTO approveRequestProductReview(Long id) {
        ReviewRequest reviewRequest = reviewRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review Request with this id not found"));
        reviewRequest.setStatus(ReviewRequestStatus.APPROVED);
        ReviewRequest savedReview = reviewRequestRepository.save(reviewRequest);

        return convertToReviewRequestDTO(savedReview);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ReviewRequestDTO rejectRequestProductReview(Long id) {
        ReviewRequest reviewRequest = reviewRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review Request with this id not found"));
        reviewRequest.setStatus(ReviewRequestStatus.REJECTED);
        ReviewRequest savedReview = reviewRequestRepository.save(reviewRequest);

        return convertToReviewRequestDTO(savedReview);
    }

    /**
     * @return
     */
    @Override
    public List<ReviewRequestDTO> getAllPendingReviewRequests() {
        return reviewRequestRepository.findAll()
                .stream()
                .filter(reviewRequest -> reviewRequest.getStatus().equals(ReviewRequestStatus.PENDING))
                .map(this::convertToReviewRequestDTO)
                .toList();
    }

    /**
     * @return
     */
    @Override
    public List<ReviewRequestDTO> getUserAllRequestProductReview() {
        User user = this.userService.getAuthenticatedUser();
        List<ReviewRequest> reviewRequests = this.reviewRequestRepository.findByUserId(user.getId());

        return reviewRequests.stream().map(this::convertToReviewRequestDTO).toList();
    }

    @PostConstruct
    public void cleanupOnStartup() {
        log.info("Checking for rejected items to cleanup on startup...");
        this.cleanupRejectedItems();
    }

    @Transactional
    public void cleanupRejectedItems() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(5);

        List<ReviewRequest> rejectedRequests = reviewRequestRepository
                .findByStatusAndRejectedAtBefore(ReviewRequestStatus.REJECTED, cutoff);

        if (!rejectedRequests.isEmpty()) {
            reviewRequestRepository.deleteAll(rejectedRequests);
            log.info("Deleted {} rejected review requests", rejectedRequests.size());
        }

        List<Review> rejectedReviews = reviewRepository
                .findByStatusAndRejectedAtBefore(ReviewStatus.REJECTED, cutoff);

        if (!rejectedReviews.isEmpty()) {
            reviewRepository.deleteAll(rejectedReviews);
            log.info("Deleted {} rejected reviews", rejectedReviews.size());
        }
    }
}
