package com.studentmanagement.config;

import com.studentmanagement.entity.User;
import com.studentmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ USER NOT FOUND: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        System.out.println("✅ USER FOUND: " + user.getEmail());
        System.out.println("🔑 Password in DB: " + user.getPassword());
        System.out.println("🔑 Password starts with $2a$: " + (user.getPassword().startsWith("$2a$")));
        System.out.println("👤 Role: " + user.getRole());
        System.out.println("✅ Enabled: " + user.isEnabled());
        System.out.println("=====================");

        if (!user.isEnabled()) {
            System.out.println("❌ USER ACCOUNT DISABLED: " + email);
            throw new UsernameNotFoundException("User account is disabled");
        }

        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}