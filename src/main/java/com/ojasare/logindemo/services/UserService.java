package com.ojasare.logindemo.services;


import com.ojasare.logindemo.dtos.UserDTO;
import com.ojasare.logindemo.models.User;

import java.util.List;

public interface UserService {
    void updateUserRole(Long userId, String roleName);

    List<User> getAllUsers();

    UserDTO getUserById(Long id);

    User findByUsername(String username);

    void generatePasswordResetToken(String email);

    void resetPassword(String token, String newPassword);
}
