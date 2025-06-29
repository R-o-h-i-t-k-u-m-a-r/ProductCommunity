package com.productcommunity.service.product;

import com.productcommunity.dto.*;
import com.productcommunity.enums.ReviewStatus;
import com.productcommunity.model.*;
import com.productcommunity.repository.ProductImageRepository;
import com.productcommunity.repository.ProductRepository;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.request.AddProductRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ModelMapper modelMapper;


    @Override
    public Product addProduct(AddProductRequest request) {
        if (productExists(request.getName(), request.getBrand())) {
            throw new AlreadyExistsException(request.getName() + " " + request.getBrand() + " Already exists, you may update this product instead!!!");
        }
        //return productRepository.save(createProduct(request));

        Product product = new Product(
                request.getName(),
                request.getCode(),
                request.getBrand(),
                request.getDescription()
        );

        // Explicitly initialize collections to prevent orphan removal error
        product.setImages(new ArrayList<>());
        product.setReviews(new ArrayList<>());

        return productRepository.save(product);
    }

    private Product createProduct(AddProductRequest request) {
        return new Product(
                request.getName(),
                request.getCode(),
                request.getBrand(),
                request.getDescription()
        );
    }

    private boolean productExists(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }

    @Override
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
        return filterApprovedReviews(product);


    }

    @Transactional
    @Override
    public void deleteProductById(Long id) {
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete,
                        () -> {
                            throw new ResourceNotFoundException("Product not found!");
                        });
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return getConvertedProducts(productRepository.findAll());
    }

    /**
     * @return
     */
    @Override
    public List<ProductDTO> getAllApprovedReviewsProducts() {

        return productRepository.findAll().stream()
                .filter(product -> !product.getReviews().isEmpty())
                .map(this::filterApprovedReviews)
                //.filter(product -> !product.getReviews().isEmpty())
                .map(this::convertToDto)
                .collect(toList());



    }

    @Override
    public Product getProductByCode(String productCode) {
        Product product = productRepository.findByCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with this code"));
        return filterApprovedReviews(product);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand).stream()
                .map(this::filterApprovedReviews)
                .collect(toList());
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name).stream()
                .map(this::filterApprovedReviews)
                .collect(toList());
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name).stream()
                .map(this::filterApprovedReviews)
                .collect(toList());
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDTO> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDTO convertToDto(Product product) {


        ProductDTO productDto = modelMapper.map(product, ProductDTO.class);


        List<ProductImage> images = imageRepository.findByProductId(product.getId());
        List<ProductImageDTO> imageDtos = images.stream()
                .map(image -> modelMapper.map(image, ProductImageDTO.class))
                .toList();
        productDto.setImages(imageDtos);

        // Map reviews with nested user information
        List<Review> reviews = product.getReviews();
        List<ReviewDTO> reviewDtos = reviews.stream()
                .map(this::convertReviewToDto)
                .toList();
        productDto.setReviews(reviewDtos);

        if (product.getReviews() == null || product.getReviews().isEmpty()) {
            productDto.setAverageRating(0.0f);
            productDto.setReviewCount(0);
        } else {

            List<Review> approvedReviews = product.getReviews().stream()
                    .filter(review -> review.getStatus() == ReviewStatus.APPROVED)
                    .collect(toList());

            if (approvedReviews.isEmpty()) {
                productDto.setAverageRating(0.0f);
                productDto.setReviewCount(0);
            } else {

                float avgRating = (float) approvedReviews.stream()
                        .mapToDouble(Review::getRating)
                        .average()
                        .orElse(0.0);

                productDto.setAverageRating(avgRating);
                productDto.setReviewCount(approvedReviews.size());
            }
        }

        return productDto;
    }

    public Product filterApprovedReviews(Product product) {
        if (product.getReviews() != null) {
            List<Review> approvedReviews = product.getReviews().stream()
                    .filter(review -> review.getStatus() == ReviewStatus.APPROVED)
                    .collect(toList());
            product.setReviews(approvedReviews);
        }
        return product;
    }

    private ReviewDTO convertReviewToDto(Review review) {
        ReviewDTO reviewDto = modelMapper.map(review, ReviewDTO.class);

        // Map user information
        User user = review.getUser();
        UserDTO userDto = modelMapper.map(user, UserDTO.class);

        // Map user image if exists
        if (user.getUserImage() != null) {
            UserImageDto userImageDto = modelMapper.map(user.getUserImage(), UserImageDto.class);
            userDto.setUserImage(userImageDto);
        }

        // Map roles
        userDto.setRoles(user.getRoles());

        reviewDto.setUserDTO(userDto);
        return reviewDto;
    }

    @Override
    public List<ProductInfoDto> getAllProductsWithReviewStats() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(this::convertToProductInfoDto)
                .collect(toList());
    }

    private ProductInfoDto convertToProductInfoDto(Product product) {
        ProductInfoDto dto = new ProductInfoDto();


        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCode(product.getCode());
        dto.setDescription(product.getDescription());


        List<ProductImage> images = imageRepository.findByProductId(product.getId());
        List<ProductImageDTO> imageDtos = images.stream()
                .map(image -> modelMapper.map(image, ProductImageDTO.class))
                .toList();
        dto.setImages(imageDtos);


        if (product.getReviews() == null || product.getReviews().isEmpty()) {
            dto.setAvgRating(0.0f);
            dto.setTotalReveiws(0);
        } else {

            List<Review> approvedReviews = product.getReviews().stream()
                    .filter(review -> review.getStatus() == ReviewStatus.APPROVED)
                    .collect(toList());

            if (approvedReviews.isEmpty()) {
                dto.setAvgRating(0.0f);
                dto.setTotalReveiws(0);
            } else {

                float avgRating = (float) approvedReviews.stream()
                        .mapToDouble(Review::getRating)
                        .average()
                        .orElse(0.0);

                dto.setAvgRating(avgRating);
                dto.setTotalReveiws(approvedReviews.size());
            }
        }

        return dto;
    }

}
