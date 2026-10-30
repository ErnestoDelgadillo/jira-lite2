package com.jiralite.config;

import com.jiralite.domain.Role;
import com.jiralite.domain.User;
import com.jiralite.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN);
            User manager = new User("manager", passwordEncoder.encode("manager123"), Role.MANAGER);
            User developer = new User("developer", passwordEncoder.encode("developer123"), Role.DEVELOPER);
            User viewer = new User("viewer", passwordEncoder.encode("viewer123"), Role.VIEWER);

            userRepository.save(admin);
            userRepository.save(manager);
            userRepository.save(developer);
            userRepository.save(viewer);
        }
    }
}

