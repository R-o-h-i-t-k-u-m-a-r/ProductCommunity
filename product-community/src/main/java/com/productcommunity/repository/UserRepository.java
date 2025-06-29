package com.productcommunity.repository;

import com.productcommunity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByUserName(String userName);
    User findByUserName(String userName);
    void deleteByUserName(String userName);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);
}
