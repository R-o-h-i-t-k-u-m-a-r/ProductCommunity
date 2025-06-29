package com.productcommunity.testcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.productcommunity.controller.ProductController;
import com.productcommunity.dto.ProductDTO;
import com.productcommunity.dto.ProductInfoDto;
import com.productcommunity.dto.ProductImageDTO;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.Product;
import com.productcommunity.request.AddProductRequest;
import com.productcommunity.response.ApiResponse;
import com.productcommunity.service.product.IProductService;
import com.productcommunity.service.productimage.IProductImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled("tested")
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private IProductService productService;

    @Mock
    private IProductImageService productImageService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;
    private ProductDTO testProductDto;
    private ProductInfoDto testProductInfoDto;
    private ProductImageDTO testProductImageDto;
    private AddProductRequest testAddProductRequest;

    @BeforeEach
    void setUp() {
        // Setup Product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setCode("TEST123");
        testProduct.setBrand("Test Brand");
        testProduct.setDescription("Test Description");

        // Setup ProductDTO
        testProductDto = new ProductDTO();
        testProductDto.setId(1L);
        testProductDto.setName("Test Product");
        testProductDto.setCode("TEST123");
        testProductDto.setBrand("Test Brand");
        testProductDto.setDescription("Test Description");

        // Setup ProductImageDTO
        testProductImageDto = new ProductImageDTO();
        testProductImageDto.setId(1L);
        testProductImageDto.setFileName("test-image.jpg");
        testProductImageDto.setDownloadUrl("http://example.com/images/test-image.jpg");

        // Setup ProductInfoDto
        testProductInfoDto = new ProductInfoDto();
        testProductInfoDto.setId(1L);
        testProductInfoDto.setName("Test Product");
        testProductInfoDto.setBrand("Test Brand");
        testProductInfoDto.setCode("TEST123");
        testProductInfoDto.setDescription("Test Description");
        testProductInfoDto.setAvgRating(4.5f);
        testProductInfoDto.setTotalReveiws(10);
        testProductInfoDto.setImages(Collections.singletonList(testProductImageDto));

        // Setup AddProductRequest
        testAddProductRequest = new AddProductRequest();
        testAddProductRequest.setName("Test Product");
        testAddProductRequest.setCode("TEST123");
        testAddProductRequest.setBrand("Test Brand");
        testAddProductRequest.setDescription("Test Description");
    }

    @Disabled("tested")
    @Test
    void getAllProductsInfo_Success() {
        // Arrange
        List<ProductInfoDto> productList = Collections.singletonList(testProductInfoDto);
        when(productService.getAllProductsWithReviewStats()).thenReturn(productList);

        // Act
        ResponseEntity<ApiResponse> response = productController.getAllProductsIfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());

        List<ProductInfoDto> responseData = (List<ProductInfoDto>) response.getBody().getData();
        assertEquals(1, responseData.size());

        ProductInfoDto responseDto = responseData.get(0);
        // Verify ProductInfoDto fields
        assertEquals(testProductInfoDto.getId(), responseDto.getId());
        assertEquals(testProductInfoDto.getName(), responseDto.getName());
        assertEquals(testProductInfoDto.getBrand(), responseDto.getBrand());
        assertEquals(testProductInfoDto.getCode(), responseDto.getCode());
        assertEquals(testProductInfoDto.getDescription(), responseDto.getDescription());
        assertEquals(testProductInfoDto.getAvgRating(), responseDto.getAvgRating());
        assertEquals(testProductInfoDto.getTotalReveiws(), responseDto.getTotalReveiws());

        // Verify ProductImageDTO fields
        assertNotNull(responseDto.getImages());
        assertEquals(1, responseDto.getImages().size());
        ProductImageDTO responseImageDto = responseDto.getImages().get(0);
        assertEquals(testProductImageDto.getId(), responseImageDto.getId());
        assertEquals(testProductImageDto.getFileName(), responseImageDto.getFileName());
        assertEquals(testProductImageDto.getDownloadUrl(), responseImageDto.getDownloadUrl());
    }

    @Disabled("tested")
    @Test
    void getAllProductsInfo_EmptyList() {
        // Arrange
        when(productService.getAllProductsWithReviewStats()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse> response = productController.getAllProductsIfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());
        assertTrue(((List<?>) response.getBody().getData()).isEmpty());
    }

    @Disabled("tested")
    @Test
    void getAllProductsInfo_Exception() {
        // Arrange
        when(productService.getAllProductsWithReviewStats()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse> response = productController.getAllProductsIfo();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Database error", response.getBody().getMessage());
    }

    @Test
    void getAllProducts_Success() {
        // Arrange
        List<ProductDTO> productList = Collections.singletonList(testProductDto);
        when(productService.getAllProducts()).thenReturn(productList);

        // Act
        ResponseEntity<ApiResponse> response = productController.getAllProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(productList, response.getBody().getData());
    }

    @Test
    void getAllApprovedReviewsProducts_Success() {
        // Arrange
        List<ProductDTO> productList = Collections.singletonList(testProductDto);
        when(productService.getAllApprovedReviewsProducts()).thenReturn(productList);

        // Act
        ResponseEntity<ApiResponse> response = productController.getAllApprovedReviewsProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(productList, response.getBody().getData());
    }

    @Test
    void getProductById_Success() throws ResourceNotFoundException {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(productService.convertToDto(testProduct)).thenReturn(testProductDto);

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(testProductDto, response.getBody().getData());
    }

    @Test
    void getProductById_NotFound() throws ResourceNotFoundException {
        // Arrange
        when(productService.getProductById(1L)).thenThrow(new ResourceNotFoundException("Product not found"));

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductById(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Product not found", response.getBody().getMessage());
    }

    @Test
    void getProductByCode_Success() throws ResourceNotFoundException {
        // Arrange
        when(productService.getProductByCode("TEST123")).thenReturn(testProduct);
        when(productService.convertToDto(testProduct)).thenReturn(testProductDto);

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductByCode("TEST123");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(testProductDto, response.getBody().getData());
    }

    @Test
    void getProductByCode_NotFound() throws ResourceNotFoundException {
        // Arrange
        when(productService.getProductByCode("INVALID")).thenThrow(new ResourceNotFoundException("Product not found"));

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductByCode("INVALID");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Product not found", response.getBody().getMessage());
    }

    @Test
    void createProduct_Success() throws JsonProcessingException, AlreadyExistsException {
        // Arrange
        String productJson = "{\"name\":\"Test Product\",\"code\":\"TEST123\"}";
        MultipartFile mockFile = new MockMultipartFile("test.jpg", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        List<MultipartFile> images = Collections.singletonList(mockFile);

        when(objectMapper.readValue(productJson, AddProductRequest.class)).thenReturn(testAddProductRequest);
        when(productService.addProduct(testAddProductRequest)).thenReturn(testProduct);
        when(productImageService.saveImages(anyLong(), anyList())).thenReturn(null);

        // Act
        ResponseEntity<ApiResponse> response = productController.createProduct(productJson, images);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product added successfully!", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        verify(productImageService, times(1)).saveImages(anyLong(), anyList());
    }

    @Test
    void createProduct_AlreadyExists() throws JsonProcessingException, AlreadyExistsException {
        // Arrange
        String productJson = "{\"name\":\"Test Product\",\"code\":\"TEST123\"}";
        when(objectMapper.readValue(productJson, AddProductRequest.class)).thenReturn(testAddProductRequest);
        when(productService.addProduct(testAddProductRequest)).thenThrow(new AlreadyExistsException("Product already exists"));

        // Act
        ResponseEntity<ApiResponse> response = productController.createProduct(productJson, null);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Product already exists", response.getBody().getMessage());
    }

    @Test
    void createProduct_InvalidJson() throws JsonProcessingException {
        // Arrange
        String invalidJson = "invalid json";
        when(objectMapper.readValue(invalidJson, AddProductRequest.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act
        ResponseEntity<ApiResponse> response = productController.createProduct(invalidJson, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid JSON Format", response.getBody().getMessage());
    }

    @Test
    void deleteProduct_Success() throws ResourceNotFoundException {
        // Arrange
        doNothing().when(productService).deleteProductById(1L);

        // Act
        ResponseEntity<ApiResponse> response = productController.deleteProduct(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete product success!", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData());
    }

    @Test
    void deleteProduct_NotFound() throws ResourceNotFoundException {
        // Arrange
        doThrow(new ResourceNotFoundException("Product not found")).when(productService).deleteProductById(1L);

        // Act
        ResponseEntity<ApiResponse> response = productController.deleteProduct(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Product not found", response.getBody().getMessage());
    }

    @Test
    void getProductByBrandAndName_Success() {
        // Arrange
        List<Product> products = Collections.singletonList(testProduct);
        List<ProductDTO> productDTOs = Collections.singletonList(testProductDto);

        when(productService.getProductsByBrandAndName("Test Brand", "Test Product")).thenReturn(products);
        when(productService.getConvertedProducts(products)).thenReturn(productDTOs);

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductByBrandAndName("Test Brand", "Test Product");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(productDTOs, response.getBody().getData());
    }

    @Test
    void getProductByBrandAndName_NotFound() {
        // Arrange
        when(productService.getProductsByBrandAndName("Unknown", "Unknown")).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductByBrandAndName("Unknown", "Unknown");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No products found ", response.getBody().getMessage());
    }

    @Test
    void getProductByName_Success() {
        // Arrange
        List<Product> products = Collections.singletonList(testProduct);
        List<ProductDTO> productDTOs = Collections.singletonList(testProductDto);

        when(productService.getProductsByName("Test Product")).thenReturn(products);
        when(productService.getConvertedProducts(products)).thenReturn(productDTOs);

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductByName("Test Product");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(productDTOs, response.getBody().getData());
    }

    @Test
    void getProductByName_NotFound() {
        // Arrange
        when(productService.getProductsByName("Unknown")).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductByName("Unknown");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No products found ", response.getBody().getMessage());
    }

    @Test
    void findProductByBrand_Success() {
        // Arrange
        List<Product> products = Collections.singletonList(testProduct);
        List<ProductDTO> productDTOs = Collections.singletonList(testProductDto);

        when(productService.getProductsByBrand("Test Brand")).thenReturn(products);
        when(productService.getConvertedProducts(products)).thenReturn(productDTOs);

        // Act
        ResponseEntity<ApiResponse> response = productController.findProductByBrand("Test Brand");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(productDTOs, response.getBody().getData());
    }

    @Test
    void countProductsByBrandAndName_Success() {
        // Arrange
        when(productService.countProductsByBrandAndName("Test Brand", "Test Product")).thenReturn(5L);

        // Act
        ResponseEntity<ApiResponse> response = productController.countProductsByBrandAndName("Test Brand", "Test Product");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product count!", response.getBody().getMessage());
        assertEquals(5L, response.getBody().getData());
    }
}