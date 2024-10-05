package com.ojasare.logindemo.repositories;


import com.ojasare.logindemo.models.AppRole;
import com.ojasare.logindemo.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
