package com.productcommunity.testcontroller;

import com.productcommunity.controller.ReviewController;
import com.productcommunity.dto.ReviewRequestDTO;
import com.productcommunity.dto.UserDTO;
import com.productcommunity.enums.ReviewRequestStatus;
import com.productcommunity.enums.ReviewStatus;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.request.CreateReviewRequest;
import com.productcommunity.request.ProductReviewRequest;
import com.productcommunity.response.ApiResponse;
import com.productcommunity.response.ReviewResponse;
import com.productcommunity.service.review.IReviewService;
import com.productcommunity.service.user.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Disabled("tested")
@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private IReviewService reviewService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private ReviewController reviewController;

    private UserDetails userDetails;
    private UserDTO userDTO;
    private ReviewResponse reviewResponse;
    private ProductReviewRequest productReviewRequest;
    private CreateReviewRequest createReviewRequest;
    private ReviewRequestDTO reviewRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup UserDetails
        userDetails = User.withUsername("1") // Using ID as username for simplicity
                .password("password")
                .authorities("USER")
                .build();

        // Setup UserDTO
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUserName("testuser");

        // Setup ProductReviewRequest
        productReviewRequest = new ProductReviewRequest();
        productReviewRequest.setProductId(1L);
        productReviewRequest.setRating(5);
        productReviewRequest.setTitle("Excellent Product");
        productReviewRequest.setContent("This product exceeded all my expectations in every way possible.");

        // Setup ReviewResponse
        reviewResponse = new ReviewResponse();
        reviewResponse.setId(1L);
        reviewResponse.setProductId(1L);
        reviewResponse.setProductName("Test Product");
        reviewResponse.setUserId(1L);
        reviewResponse.setUserName("testuser");
        reviewResponse.setUserAvatar("http://example.com/avatar.jpg");
        reviewResponse.setRating(5);
        reviewResponse.setTitle("Excellent Product");
        reviewResponse.setContent("This product exceeded all my expectations in every way possible.");
        reviewResponse.setStatus(ReviewStatus.APPROVED);
        reviewResponse.setCreatedAt(LocalDateTime.now());
        reviewResponse.setUpdatedAt(LocalDateTime.now());

        // Setup CreateReviewRequest
        createReviewRequest = new CreateReviewRequest();
        createReviewRequest.setProductCode("101");
        createReviewRequest.setProductName("demo");
        createReviewRequest.setProductBrand("test");

        // Setup ReviewRequestDTO
        reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setId(1L);
        reviewRequestDTO.setProductCode("101");
        reviewRequestDTO.setProductName("demo");
        reviewRequestDTO.setProductBrand("test");
        reviewRequestDTO.setStatus(ReviewRequestStatus.PENDING);
    }

    @Test
    void createReview_Success() throws ResourceNotFoundException {
        // Arrange
        when(userService.getByUserName("1")).thenReturn(userDTO);
        when(reviewService.createReview(any(ProductReviewRequest.class), anyLong())).thenReturn(reviewResponse);

        // Act
        ResponseEntity<ApiResponse> response = reviewController.createReview(productReviewRequest, userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("review created ", response.getBody().getMessage());

        ReviewResponse responseData = (ReviewResponse) response.getBody().getData();
        assertNotNull(responseData);
        assertEquals(reviewResponse.getId(), responseData.getId());
        assertEquals(reviewResponse.getProductId(), responseData.getProductId());
        assertEquals(reviewResponse.getProductName(), responseData.getProductName());
        assertEquals(reviewResponse.getUserId(), responseData.getUserId());
        assertEquals(reviewResponse.getUserName(), responseData.getUserName());
        assertEquals(reviewResponse.getUserAvatar(), responseData.getUserAvatar());
        assertEquals(reviewResponse.getRating(), responseData.getRating());
        assertEquals(reviewResponse.getTitle(), responseData.getTitle());
        assertEquals(reviewResponse.getContent(), responseData.getContent());
        assertEquals(reviewResponse.getStatus(), responseData.getStatus());
        assertNotNull(responseData.getCreatedAt());
        assertNotNull(responseData.getUpdatedAt());
    }

    @Test
    void updateReview_Success() {
        // Arrange
        when(reviewService.updateReview(anyLong(), any(ProductReviewRequest.class), anyLong())).thenReturn(reviewResponse);

        // Act
        ResponseEntity<ReviewResponse> response = reviewController.updateReview(1L, productReviewRequest, userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(reviewResponse.getId(), responseBody.getId());
        assertEquals(reviewResponse.getProductId(), responseBody.getProductId());
        assertEquals(reviewResponse.getRating(), responseBody.getRating());
        assertEquals(reviewResponse.getTitle(), responseBody.getTitle());
        assertEquals(reviewResponse.getContent(), responseBody.getContent());
    }

    @Test
    void getProductReviews_Success() {
        // Arrange
        Page<ReviewResponse> page = new PageImpl<>(Collections.singletonList(reviewResponse));
        when(reviewService.getApprovedReviewsByProduct(anyLong(), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<ReviewResponse>> response =
                reviewController.getProductReviews(1L, Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<ReviewResponse> responsePage = response.getBody();
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());

        ReviewResponse firstReview = responsePage.getContent().get(0);
        assertEquals(reviewResponse.getId(), firstReview.getId());
        assertEquals(reviewResponse.getProductId(), firstReview.getProductId());
        assertEquals(reviewResponse.getRating(), firstReview.getRating());
        assertEquals(reviewResponse.getTitle(), firstReview.getTitle());
        assertEquals(reviewResponse.getContent(), firstReview.getContent());
        assertEquals(reviewResponse.getStatus(), firstReview.getStatus());
    }

    @Test
    void getProductReviewStats_Success() {
        // Arrange
        when(reviewService.getProductAverageRating(anyLong())).thenReturn(4.8);
        when(reviewService.countApprovedReviewsForProduct(anyLong())).thenReturn(25L);

        // Act
        ResponseEntity<Map<String, Object>> response = reviewController.getProductReviewStats(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> stats = response.getBody();
        assertNotNull(stats);
        assertEquals(4.8, stats.get("averageRating"));
        assertEquals(25L, stats.get("reviewCount"));
    }

    // Additional tests for edge cases
    @Test
    void createReview_WithMinimumContentLength() throws ResourceNotFoundException {
        // Arrange
        ProductReviewRequest minimalRequest = new ProductReviewRequest();
        minimalRequest.setProductId(1L);
        minimalRequest.setRating(1);
        minimalRequest.setTitle("Minimal");
        minimalRequest.setContent("Exactly 20 characters!!"); // 20 characters

        ReviewResponse minimalResponse = new ReviewResponse();
        minimalResponse.setId(2L);
        minimalResponse.setRating(1);
        minimalResponse.setTitle("Minimal");
        minimalResponse.setContent("Exactly 20 characters!!");

        when(userService.getByUserName("1")).thenReturn(userDTO);
        when(reviewService.createReview(any(ProductReviewRequest.class), anyLong())).thenReturn(minimalResponse);

        // Act
        ResponseEntity<ApiResponse> response = reviewController.createReview(minimalRequest, userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewResponse responseData = (ReviewResponse) response.getBody().getData();
        assertEquals("Exactly 20 characters!!", responseData.getContent());
    }

    @Test
    void createReview_WithMaximumContentLength() throws ResourceNotFoundException {
        // Arrange
        String maxContent = "a".repeat(400); // 400 characters
        ProductReviewRequest maxRequest = new ProductReviewRequest();
        maxRequest.setProductId(1L);
        maxRequest.setRating(5);
        maxRequest.setTitle("Max Length");
        maxRequest.setContent(maxContent);

        ReviewResponse maxResponse = new ReviewResponse();
        maxResponse.setId(3L);
        maxResponse.setRating(5);
        maxResponse.setTitle("Max Length");
        maxResponse.setContent(maxContent);

        when(userService.getByUserName("1")).thenReturn(userDTO);
        when(reviewService.createReview(any(ProductReviewRequest.class), anyLong())).thenReturn(maxResponse);

        // Act
        ResponseEntity<ApiResponse> response = reviewController.createReview(maxRequest, userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewResponse responseData = (ReviewResponse) response.getBody().getData();
        assertEquals(400, responseData.getContent().length());
    }




    @Test
    void createReview_ProductNotFound() throws ResourceNotFoundException {
        when(userService.getByUserName("1")).thenReturn(userDTO);
        when(reviewService.createReview(any(), anyLong())).thenThrow(new ResourceNotFoundException("Product not found"));

        ResponseEntity<ApiResponse> response = reviewController.createReview(productReviewRequest, userDetails);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Product not found", response.getBody().getMessage());
    }

    @Test
    void createReview_IllegalState() throws ResourceNotFoundException {
        when(userService.getByUserName("1")).thenReturn(userDTO);
        when(reviewService.createReview(any(), anyLong())).thenThrow(new IllegalStateException("Cannot review own product"));

        ResponseEntity<ApiResponse> response = reviewController.createReview(productReviewRequest, userDetails);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("Cannot review own product", response.getBody().getMessage());
    }



    @Test
    void getUserReviews_Success() throws ResourceNotFoundException {
        when(userService.getByUserName("1")).thenReturn(userDTO);
        Page<ReviewResponse> page = new PageImpl<>(Collections.singletonList(reviewResponse));
        when(reviewService.getUserReviews(anyLong(), any())).thenReturn(page);

        ResponseEntity<Page<ReviewResponse>> response = reviewController.getUserReviews(userDetails, Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(reviewResponse, response.getBody().getContent().get(0));
    }


    @Test
    void deleteReview_Success() {
        ResponseEntity<Void> response = reviewController.deleteReview(1L, userDetails);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reviewService, times(1)).deleteReview(1L, 1L);
    }

    @Test
    void approveReview_Success() throws ResourceNotFoundException {
        when(reviewService.approveReview(anyLong())).thenReturn(reviewResponse);

        ResponseEntity<ApiResponse> response = reviewController.approveReview(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("review created ", response.getBody().getMessage());
        assertEquals(reviewResponse, response.getBody().getData());
    }

    @Test
    void approveReview_NotFound() throws ResourceNotFoundException {
        when(reviewService.approveReview(anyLong())).thenThrow(new ResourceNotFoundException("Review not found"));

        ResponseEntity<ApiResponse> response = reviewController.approveReview(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Review not found", response.getBody().getMessage());
    }

    @Test
    void rejectReview_Success() throws ResourceNotFoundException {
        when(reviewService.rejectReview(anyLong(), anyString())).thenReturn(reviewResponse);

        ResponseEntity<ApiResponse> response = reviewController.rejectReview(1L, "Invalid content");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("review created ", response.getBody().getMessage());
        assertEquals(reviewResponse, response.getBody().getData());
    }

    @Test
    void getAllPendingReviews_Success() {
        when(reviewService.allPendingReviews()).thenReturn(Collections.singletonList(reviewResponse));

        ResponseEntity<ApiResponse> response = reviewController.getAllPendingReviews();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("pending reviews fetched ", response.getBody().getMessage());
        assertEquals(1, ((List<?>) response.getBody().getData()).size());
    }

    @Test
    void requestProductReview_Success() throws AlreadyExistsException {
        when(reviewService.requestProductReview(any())).thenReturn(reviewRequestDTO);

        ResponseEntity<ApiResponse> response = reviewController.requestProductReview(createReviewRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("review quest created", response.getBody().getMessage());
        assertEquals(reviewRequestDTO, response.getBody().getData());
    }

    @Test
    void requestProductReview_AlreadyExists() throws AlreadyExistsException {
        when(reviewService.requestProductReview(any())).thenThrow(new AlreadyExistsException("Request already exists"));

        ResponseEntity<ApiResponse> response = reviewController.requestProductReview(createReviewRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Request already exists", response.getBody().getMessage());
    }

    @Disabled("tested")
    @Test
    void getAllRequestProductReviews_Success() {
        when(reviewService.getAllReviewRequests()).thenReturn(Collections.singletonList(reviewRequestDTO));

        ResponseEntity<ApiResponse> response = reviewController.getAllRequestProductReviews();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fetched all review requests", response.getBody().getMessage());
        assertEquals(1, ((List<?>) response.getBody().getData()).size());
    }


    @Disabled("tested")
    @Test
    void approveReviewRequest_Success() throws ResourceNotFoundException {
        when(reviewService.approveRequestProductReview(anyLong())).thenReturn(reviewRequestDTO);

        ResponseEntity<ApiResponse> response = reviewController.approveReviewRequest(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("review created ", response.getBody().getMessage());
        assertEquals(reviewRequestDTO, response.getBody().getData());
    }

    @Disabled("tested")
    @Test
    void rejectReviewRequest_Success() throws ResourceNotFoundException {
        when(reviewService.rejectRequestProductReview(anyLong())).thenReturn(reviewRequestDTO);

        ResponseEntity<ApiResponse> response = reviewController.rejectReviewRequest(1L, "Invalid");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("review created ", response.getBody().getMessage());
        assertEquals(reviewRequestDTO, response.getBody().getData());
    }

    @Disabled("tested")
    @Test
    void getAllPendingReviewRequests_Success() {
        when(reviewService.getAllPendingReviewRequests()).thenReturn(Collections.singletonList(reviewRequestDTO));

        ResponseEntity<ApiResponse> response = reviewController.getAllPendingReviewRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("pending reviews fetched ", response.getBody().getMessage());
        assertEquals(1, ((List<?>) response.getBody().getData()).size());
    }

    @Disabled("tested")
    @Test
    void getAllUserReviewRequests_Success() {
        when(reviewService.getUserAllRequestProductReview()).thenReturn(Collections.singletonList(reviewRequestDTO));

        ResponseEntity<ApiResponse> response = reviewController.getAllUserReviewRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("all reviews requests found ", response.getBody().getMessage());
        assertEquals(1, ((List<?>) response.getBody().getData()).size());
    }
}
