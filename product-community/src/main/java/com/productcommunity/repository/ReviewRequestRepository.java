package com.productcommunity.repository;

import com.productcommunity.enums.ReviewRequestStatus;
import com.productcommunity.model.ReviewRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRequestRepository extends JpaRepository<ReviewRequest,Long> {
    List<ReviewRequest> findByUserId(Long userId);

    List<ReviewRequest> findByStatusAndRejectedAtBefore(
            ReviewRequestStatus status,
            LocalDateTime rejectedBefore
    );
}
