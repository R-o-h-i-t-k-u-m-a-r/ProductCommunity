package com.productcommunity.service.product;

import com.productcommunity.dto.ProductDTO;
import com.productcommunity.dto.ProductInfoDto;
import com.productcommunity.dto.ReviewRequestDTO;
import com.productcommunity.model.Product;
import com.productcommunity.request.AddProductRequest;
import com.productcommunity.request.CreateReviewRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductService {
    Product addProduct(AddProductRequest product);
    Product getProductById(Long id);
    void deleteProductById(Long id);
    List<ProductDTO> getAllProducts();

    List<ProductDTO> getAllApprovedReviewsProducts();

    Product getProductByCode(String productCode);
    List<Product> getProductsByBrand(String brand);
    List<Product> getProductsByName(String name);
    List<Product> getProductsByBrandAndName(String category, String name);
    Long countProductsByBrandAndName(String brand, String name);


    List<ProductDTO> getConvertedProducts(List<Product> products);

    ProductDTO convertToDto(Product product);

    List<ProductInfoDto> getAllProductsWithReviewStats();


}
