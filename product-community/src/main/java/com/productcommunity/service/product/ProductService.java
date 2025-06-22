package com.productcommunity.service.product;

import com.productcommunity.dto.*;
import com.productcommunity.model.Review;
import com.productcommunity.model.User;
import com.productcommunity.repository.ProductImageRepository;
import com.productcommunity.repository.ProductRepository;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.Product;
import com.productcommunity.model.ProductImage;
import com.productcommunity.request.AddProductRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return productRepository.save(createProduct(request));
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
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
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

    @Override
    public Product getProductByCode(String productCode) {
        return productRepository.findByCode(productCode)
                .orElseThrow(()->new ResourceNotFoundException("Product not found with this code"));
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
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
        return productDto;
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
}
