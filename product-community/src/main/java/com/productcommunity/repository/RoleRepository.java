package com.productcommunity.repository;

import com.productcommunity.enums.ERole;
import com.productcommunity.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    //Role findByName(String role);
    //Boolean existsByName(String role);

    Optional<Role> findByName(ERole name);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name")
    boolean existsByName(ERole name);
}
