package com.studentmanagement.config;

import com.studentmanagement.entity.User;
import com.studentmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Clear any existing test users to avoid conflicts
        userRepository.findByEmail("admin@student.com").ifPresent(user -> userRepository.delete(user));
        userRepository.findByEmail("user@student.com").ifPresent(user -> userRepository.delete(user));

        // Create admin user
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@student.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        User savedAdmin = userRepository.save(admin);

        // Create regular user - THIS WAS MISSING!
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("user@student.com");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRole("USER");
        user.setEnabled(true);
        User savedUser = userRepository.save(user);

        System.out.println("=========================================");
        System.out.println("USERS CREATED SUCCESSFULLY!");
        System.out.println("=========================================");
        System.out.println("Admin User:");
        System.out.println("Email: " + savedAdmin.getEmail());
        System.out.println("Password: admin123");
        System.out.println("Role: " + savedAdmin.getRole());
        System.out.println("Enabled: " + savedAdmin.isEnabled());
        System.out.println("=========================================");
        System.out.println("Regular User:");
        System.out.println("Email: " + savedUser.getEmail());
        System.out.println("Password: user123");
        System.out.println("Role: " + savedUser.getRole());
        System.out.println("Enabled: " + savedUser.isEnabled());
        System.out.println("=========================================");

        // Verify the users were saved
        long userCount = userRepository.count();
        System.out.println("Total users in database: " + userCount);
    }
}