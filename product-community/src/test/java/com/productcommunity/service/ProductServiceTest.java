package com.productcommunity.service;

import com.productcommunity.dto.*;
import com.productcommunity.enums.ReviewStatus;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.*;
import com.productcommunity.repository.ProductImageRepository;
import com.productcommunity.repository.ProductRepository;
import com.productcommunity.request.AddProductRequest;
import com.productcommunity.service.product.ProductService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository imageRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductService productService;

    @Disabled("tested")
    @Test
    void addProduct_WhenProductDoesNotExist_ShouldSaveProduct() {
        // Arrange
        AddProductRequest request = new AddProductRequest();
        request.setName("Test Product");
        request.setBrand("Test Brand");
        request.setCode("TEST123");
        request.setDescription("Test Description");

        when(productRepository.existsByNameAndBrand(anyString(), anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = productService.addProduct(request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Test Brand", result.getBrand());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Disabled("tested")
    @Test
    void addProduct_WhenProductExists_ShouldThrowAlreadyExistsException() {
        // Arrange
        AddProductRequest request = new AddProductRequest();
        request.setName("Existing Product");
        request.setBrand("Existing Brand");

        when(productRepository.existsByNameAndBrand(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> productService.addProduct(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Disabled("tested")
    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setReviews(new ArrayList<>());

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act
        Product result = productService.getProductById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
    }

    @Disabled("tested")
    @Test
    void getProductById_WhenProductNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Disabled("tested")
    @Test
    void deleteProductById_WhenProductExists_ShouldDeleteProduct() {
        // Arrange
        Long productId = 1L;
        Product mockProduct = new Product();
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act
        productService.deleteProductById(productId);

        // Assert
        verify(productRepository, times(1)).delete(mockProduct);
    }

    @Disabled("tested")
    @Test
    void deleteProductById_WhenProductNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProductById(productId));
        verify(productRepository, never()).delete(any());
    }

    @Disabled("tested")
    @Test
    void getAllProducts_ShouldReturnListOfProductDTOs() {
        // Arrange
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productRepository.findAll()).thenReturn(products);
        when(modelMapper.map(any(), eq(ProductDTO.class))).thenReturn(new ProductDTO());

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Disabled("tested")
    @Test
    void getAllApprovedReviewsProducts_ShouldReturnOnlyProductsWithReviews() {
        // Arrange
        Product withReviews = new Product();
        withReviews.setReviews(Arrays.asList(new Review(), new Review()));

        Product withoutReviews = new Product();
        withoutReviews.setReviews(Collections.emptyList());

        when(productRepository.findAll()).thenReturn(Arrays.asList(withReviews, withoutReviews));
        when(modelMapper.map(any(), eq(ProductDTO.class))).thenReturn(new ProductDTO());

        // Act
        List<ProductDTO> result = productService.getAllApprovedReviewsProducts();

        // Assert
        assertEquals(1, result.size());
    }

    @Disabled("tested")
    @Test
    void getProductByCode_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        String productCode = "TEST123";
        Product mockProduct = new Product();
        mockProduct.setCode(productCode);
        mockProduct.setReviews(new ArrayList<>());

        when(productRepository.findByCode(productCode)).thenReturn(Optional.of(mockProduct));

        // Act
        Product result = productService.getProductByCode(productCode);

        // Assert
        assertNotNull(result);
        assertEquals(productCode, result.getCode());
    }

    @Disabled("tested")
    @Test
    void convertToDto_ShouldMapProductToDtoCorrectly() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setReviews(Arrays.asList(
                createReview(5, ReviewStatus.APPROVED),
                createReview(3, ReviewStatus.APPROVED),
                createReview(4, ReviewStatus.PENDING)
        ));

        ProductImage image = new ProductImage();
        product.setImages(List.of(image));

        when(imageRepository.findByProductId(anyLong())).thenReturn(Arrays.asList(image));
        when(modelMapper.map(any(), eq(ProductDTO.class))).thenReturn(new ProductDTO());
        when(modelMapper.map(any(), eq(ProductImageDTO.class))).thenReturn(new ProductImageDTO());
        when(modelMapper.map(any(),eq(UserDTO.class))).thenReturn(new UserDTO());
        when(modelMapper.map(any(), eq(ReviewDTO.class))).thenReturn(new ReviewDTO());

        // Act
        ProductDTO result = productService.convertToDto(product);

        // Assert
        assertNotNull(result);
        assertEquals(4.0f, result.getAverageRating()); // (5+3)/2 = 4
        assertEquals(2, result.getReviewCount());
        verify(imageRepository, times(1)).findByProductId(product.getId());
    }

    @Disabled("tested")
    @Test
    void filterApprovedReviews_ShouldReturnOnlyApprovedReviews() {
        // Arrange
        Product product = new Product();
        product.setReviews(Arrays.asList(
                createReview(1, ReviewStatus.APPROVED),
                createReview(2, ReviewStatus.PENDING),
                createReview(3, ReviewStatus.REJECTED)
        ));

        // Act
        Product result = productService.filterApprovedReviews(product);

        // Assert
        assertNotNull(result.getReviews());
        assertEquals(1, result.getReviews().size());
        assertEquals(ReviewStatus.APPROVED, result.getReviews().get(0).getStatus());
    }

    @Disabled("tested")
    @Test
    void getAllProductsWithReviewStats_ShouldReturnCorrectStats() {
        // Arrange
        Product product = new Product();
        product.setReviews(Arrays.asList(
                createReview(5, ReviewStatus.APPROVED),
                createReview(3, ReviewStatus.APPROVED),
                createReview(4, ReviewStatus.PENDING)
        ));

        ProductImage image = new ProductImage();
        product.setImages(List.of(image));

        when(productRepository.findAll()).thenReturn(List.of(product));

        // Act
        List<ProductInfoDto> result = productService.getAllProductsWithReviewStats();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ProductInfoDto dto = result.get(0);
        assertEquals(4.0f, dto.getAvgRating());
        assertEquals(2, dto.getTotalReveiws());
    }

    private Review createReview(int rating, ReviewStatus status) {
        Review review = new Review();
        review.setRating(rating);
        review.setStatus(status);
        review.setUser(new User());
        return review;
    }
}
