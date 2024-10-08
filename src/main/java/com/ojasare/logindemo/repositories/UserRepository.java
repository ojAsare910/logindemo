package com.ojasare.logindemo.repositories;

import com.ojasare.logindemo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String username);
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
    Boolean existsByUserName(String username);
}
