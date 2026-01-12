package com.example.ticketing.util;

import com.example.ticketing.model.Role;
import com.example.ticketing.model.User;
import com.example.ticketing.repository.UserRepository;
import com.example.ticketing.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    public DataLoader(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").ifPresentOrElse(
                u -> {},
                () -> {
                    User admin = User.builder()
                            .username("admin")
                            .password("admin123")
                            .fullName("Administrator")
                            .roles(Set.of(Role.ROLE_ADMIN))
                            .build();
                    userService.createUser(admin);
                }
        );
    }
}
