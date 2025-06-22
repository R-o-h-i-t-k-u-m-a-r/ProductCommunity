package com.productcommunity.repository;

import com.productcommunity.model.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImageRepository extends JpaRepository<UserImage,Long> {
    UserImage findByUserId(Long userId);
}
