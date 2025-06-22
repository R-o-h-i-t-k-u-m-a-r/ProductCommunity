package com.productcommunity.repository;

import com.productcommunity.enums.ReviewStatus;
import com.productcommunity.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review,Long> {

    @EntityGraph(attributePaths = {"user", "user.roles", "user.userImage"})
    List<Review> findByProductId(Long productId);

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.user.id = :userId")
    Optional<Review> findByProductAndUser(@Param("productId") Long productId,
                                          @Param("userId") Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);

    long countByProductIdAndStatus(Long productId, ReviewStatus status);

}
