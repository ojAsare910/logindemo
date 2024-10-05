package com.ojasare.logindemo.services.impl;


import com.ojasare.logindemo.Util.EmailService;
import com.ojasare.logindemo.dtos.UserDTO;
import com.ojasare.logindemo.models.AppRole;
import com.ojasare.logindemo.models.PasswordResetToken;
import com.ojasare.logindemo.models.Role;
import com.ojasare.logindemo.models.User;
import com.ojasare.logindemo.repositories.PasswordResetTokenRepository;
import com.ojasare.logindemo.repositories.RoleRepository;
import com.ojasare.logindemo.repositories.UserRepository;
import com.ojasare.logindemo.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    @Value("${frontend.url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailService emailService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        AppRole appRole = AppRole.valueOf(roleName);
        Role role = roleRepository.findByRoleName(appRole).orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }

//    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .isTwoFactorEnabled(user.isTwoFactorEnabled())
                .accountExpiryDate(user.getAccountExpiryDate())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .enabled(user.isEnabled())
                .createdDate(user.getCreatedDate())
                .role(user.getRole())
                .updatedDate(user.getUpdatedDate())
                .signUpMethod(user.getSignUpMethod())
                .build();
    }

    @Override
    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUserName(username);
        return user.orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Override
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
        PasswordResetToken resetToken = new PasswordResetToken(token, expiryDate, user);
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);

    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));
        if (resetToken.isUsed())
            throw new RuntimeException("Password reset token has already been used");

        if (resetToken.getExpiryDate().isBefore(Instant.now()))
            throw new RuntimeException("Password reset token has expired");

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

}
