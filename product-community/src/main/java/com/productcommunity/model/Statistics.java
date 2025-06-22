package com.productcommunity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "total_users")
    private Long totalUsers = 0L;

    @Column(name = "total_products")
    private Long totalProducts = 0L;

    @Column(name = "total_reviews")
    private Long totalReviews = 0L;

    @Column(name = "pending_reviews")
    private Long pendingReviews = 0L;

    @Column(name = "online_users")
    private Integer onlineUsers = 0;

    @Column(name = "last_updated")
    @UpdateTimestamp
    private LocalDateTime lastUpdated;
}
