package com.ojasare.logindemo.security;


import com.ojasare.logindemo.models.AppRole;
import com.ojasare.logindemo.models.Role;
import com.ojasare.logindemo.models.User;
import com.ojasare.logindemo.repositories.RoleRepository;
import com.ojasare.logindemo.repositories.UserRepository;
import com.ojasare.logindemo.security.jwt.AuthEntryPointJwt;
import com.ojasare.logindemo.security.jwt.AuthTokenFilter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDate;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {


    public SecurityConfig(AuthEntryPointJwt unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

    private final AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests((requests)
                -> requests
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/auth/public/**").permitAll()
                .anyRequest().authenticated());
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_USER)));

            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_ADMIN)));

            if (!userRepository.existsByUserName("user1")) {
                User user1 = User.builder()
                        .userName("user1")
                        .email("user1@email.com")
                        .password(passwordEncoder.encode("password1"))
                        .accountNonLocked(false)
                        .accountNonExpired(true)
                        .credentialsNonExpired(true)
                        .enabled(true)
                        .credentialsExpiryDate(LocalDate.now().plusYears(1))
                        .accountExpiryDate(LocalDate.now().plusYears(1))
                        .isTwoFactorEnabled(false)
                        .signUpMethod("email")
                        .role(userRole)
                        .build();
                userRepository.save(user1);
            }

            if (!userRepository.existsByUserName("admin")) {
                User admin = User.builder()
                        .userName("admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("adminPass"))
                        .accountNonLocked(false)
                        .accountNonExpired(true)
                        .credentialsNonExpired(true)
                        .enabled(true)
                        .credentialsExpiryDate(LocalDate.now().plusYears(1))
                        .accountExpiryDate(LocalDate.now().plusYears(1))
                        .isTwoFactorEnabled(false)
                        .signUpMethod("email")
                        .role(adminRole)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}
