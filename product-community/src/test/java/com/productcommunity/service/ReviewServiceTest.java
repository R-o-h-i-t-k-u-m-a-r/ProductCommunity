package com.productcommunity.service;



import com.productcommunity.dto.ReviewRequestDTO;
import com.productcommunity.dto.UserDTO;
import com.productcommunity.enums.ReviewRequestStatus;
import com.productcommunity.enums.ReviewStatus;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.*;
import com.productcommunity.repository.*;
import com.productcommunity.request.CreateReviewRequest;
import com.productcommunity.request.ProductReviewRequest;
import com.productcommunity.response.ReviewResponse;
import com.productcommunity.service.review.ReviewService;
import com.productcommunity.service.user.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRequestRepository reviewRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Disabled("tested")
    @Test
    void createReview_WhenValidRequest_ShouldCreateReview() {
        // Arrange
        Long userId = 1L;
        Long productId = 1L;
        ProductReviewRequest request = new ProductReviewRequest();
        request.setProductId(productId);
        request.setRating(5);
        request.setTitle("Great product");
        request.setContent("Excellent quality");

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.findByProductAndUser(productId, userId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ReviewResponse response = reviewService.createReview(request, userId);

        // Assert
        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Great product", response.getTitle());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Disabled("tested")
    @Test
    void createReview_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        Long productId = 999L;
        ProductReviewRequest request = new ProductReviewRequest();
        request.setProductId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> reviewService.createReview(request, 1L));
    }

    @Disabled("tested")
    @Test
    void createReview_WhenUserAlreadyReviewed_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        Long productId = 1L;
        ProductReviewRequest request = new ProductReviewRequest();
        request.setProductId(productId);

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.findByProductAndUser(productId, userId)).thenReturn(Optional.of(new Review()));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> reviewService.createReview(request, userId));
    }

    @Disabled("tested")
    @Test
    void updateReview_WhenValidRequest_ShouldUpdateReview() {
        // Arrange
        Long reviewId = 1L;
        Long userId = 1L;
        ProductReviewRequest request = new ProductReviewRequest();
        request.setRating(4);
        request.setTitle("Updated title");
        request.setContent("Updated content");

        Review existingReview = new Review();
        existingReview.setId(reviewId);
        existingReview.setStatus(ReviewStatus.PENDING);

        User user = new User();
        user.setId(userId);
        existingReview.setUser(user);
        existingReview.setProduct(new Product());

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ReviewResponse response = reviewService.updateReview(reviewId, request, userId);

        // Assert
        assertNotNull(response);
        assertEquals(4, existingReview.getRating());
        assertEquals("Updated title", existingReview.getTitle());
        verify(reviewRepository, times(1)).save(existingReview);
    }

    @Disabled("tested")
    @Test
    void updateReview_WhenNotReviewOwner_ShouldThrowException() {
        // Arrange
        Long reviewId = 1L;
        Long userId = 2L; // Different from review owner
        ProductReviewRequest request = new ProductReviewRequest();

        Review existingReview = new Review();
        User owner = new User();
        owner.setId(1L); // Original owner
        existingReview.setUser(owner);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> reviewService.updateReview(reviewId, request, userId));
    }

    @Disabled("tested")
    @Test
    void getApprovedReviewsByProduct_ShouldReturnPageOfReviews() {
        // Arrange
        Long productId = 1L;
        Pageable pageable = Pageable.unpaged();

        Review review = new Review();
        review.setId(1L);

        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        review.setProduct(product);

        User user = new User();
        user.setId(1L);
        user.setUserName("john_doe");
        review.setUser(user);

        review.setRating(5);
        review.setTitle("Great!");
        review.setContent("Excellent product");
        review.setStatus(ReviewStatus.APPROVED);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        List<Review> reviews = Collections.singletonList(review);
        Page<Review> page = new PageImpl<>(reviews);

        when(reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable)).thenReturn(page);

        // Act
        Page<ReviewResponse> result = reviewService.getApprovedReviewsByProduct(productId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Great!", result.getContent().get(0).getTitle());
    }

    @Disabled("tested")
    @Test
    void deleteReview_WhenValidRequest_ShouldDeleteReview() {
        // Arrange
        Long reviewId = 1L;
        Long userId = 1L;

        Review review = new Review();
        User user = new User();
        user.setId(userId);
        review.setUser(user);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        // Act
        reviewService.deleteReview(reviewId, userId);

        // Assert
        verify(reviewRepository, times(1)).delete(review);
    }

    @Disabled("tested")
    @Test
    void approveReview_ShouldUpdateStatusToApproved() {
        // Arrange
        Long reviewId = 1L;

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");

        User user = new User();
        user.setId(200L);
        user.setUserName("testuser");

        Review review = new Review();
        review.setId(reviewId);
        review.setStatus(ReviewStatus.PENDING);
        review.setProduct(product);
        review.setUser(user);
        review.setRating(4);
        review.setTitle("Good");
        review.setContent("Nice product");
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);

        // Act
        ReviewResponse response = reviewService.approveReview(reviewId);

        // Assert
        assertEquals(ReviewStatus.APPROVED, review.getStatus());
        assertNotNull(response);
        assertEquals(100L, response.getProductId());
        assertEquals(200L, response.getUserId());
    }


    @Disabled("tested")
    @Test
    void requestProductReview_WhenProductNotExists_ShouldCreateRequest() {
        // Arrange
        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductCode("NEW123");
        request.setProductName("New Product");
        request.setProductBrand("New Brand");

        User user = new User();
        user.setId(1L); // Optional but good for completeness
        user.setUserName("testuser");

        ReviewRequest savedRequest = new ReviewRequest();
        savedRequest.setStatus(ReviewRequestStatus.PENDING);
        savedRequest.setUser(user); // ✅ IMPORTANT

        when(productRepository.existsByCode(request.getProductCode())).thenReturn(false);
        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(reviewRequestRepository.save(any(ReviewRequest.class))).thenReturn(savedRequest);
        when(modelMapper.map(eq(user), eq(UserDTO.class))).thenReturn(new UserDTO());

        // Act
        ReviewRequestDTO result = reviewService.requestProductReview(request);

        // Assert
        assertNotNull(result);
        assertEquals(ReviewRequestStatus.PENDING, result.getStatus());
        verify(reviewRequestRepository, times(1)).save(any(ReviewRequest.class));
    }


    @Disabled("tested")
    @Test
    void requestProductReview_WhenProductExists_ShouldThrowException() {
        // Arrange
        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductCode("EXIST123");

        when(productRepository.existsByCode(request.getProductCode())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> reviewService.requestProductReview(request));
    }

    @Disabled("tested")
    @Test
    void getAllPendingReviewRequests_ShouldReturnOnlyPendingRequests() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUserName("testuser");

        ReviewRequest pending = new ReviewRequest();
        pending.setStatus(ReviewRequestStatus.PENDING);
        pending.setProductCode("PENDING123");
        pending.setProductBrand("Brand A");
        pending.setProductName("Pending Product");
        pending.setCreatedAt(LocalDateTime.now());
        pending.setUser(user); // ✅ Required to prevent NPE in convertToReviewRequestDTO

        ReviewRequest approved = new ReviewRequest();
        approved.setStatus(ReviewRequestStatus.APPROVED);

        when(reviewRequestRepository.findAll()).thenReturn(Arrays.asList(pending, approved));
        when(modelMapper.map(eq(user), eq(UserDTO.class))).thenReturn(new UserDTO()); // also needed

        // Act
        List<ReviewRequestDTO> result = reviewService.getAllPendingReviewRequests();

        // Assert
        assertEquals(1, result.size());
    }


    @Disabled("tested")
    @Test
    void cleanupRejectedItems_ShouldDeleteOldRejectedItems() {
        ReviewRequest oldRejectedRequest = new ReviewRequest();
        oldRejectedRequest.setStatus(ReviewRequestStatus.REJECTED);
        oldRejectedRequest.setRejectedAt(LocalDateTime.now().minusDays(6));
        oldRejectedRequest.setUser(new User());

        Review oldRejectedReview = new Review();
        oldRejectedReview.setStatus(ReviewStatus.REJECTED);
        oldRejectedReview.setRejectedAt(LocalDateTime.now().minusDays(6));
        oldRejectedReview.setProduct(new Product());
        oldRejectedReview.setUser(new User());

        when(reviewRequestRepository.findByStatusAndRejectedAtBefore(
                eq(ReviewRequestStatus.REJECTED), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(oldRejectedRequest));

        when(reviewRepository.findByStatusAndRejectedAtBefore(
                eq(ReviewStatus.REJECTED), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(oldRejectedReview));

        // Act
        reviewService.cleanupRejectedItems();

        // Assert
        verify(reviewRequestRepository).deleteAll(anyList());
        verify(reviewRepository).deleteAll(anyList());
    }

}
