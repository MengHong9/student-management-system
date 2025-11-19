package com.studentmanagement.controller;

import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Add this

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserByEmail(email).orElse(null);
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
            return "register";
        }

        // FIX: Encode the password before saving
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Set default role for new registrations
        user.setRole("USER");
        user.setEnabled(true);

        userService.createUser(user);

        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = getCurrentUser();

        if (currentUser != null) {
            List<Student> recentStudents;
            long studentCount;
            long totalStudents = 0;

            if (currentUser.getRole().equals("ADMIN")) {
                recentStudents = studentService.getAllStudents();
                // Limit to 5 recent students for display
                if (recentStudents.size() > 5) {
                    recentStudents = recentStudents.subList(0, 5);
                }
                studentCount = studentService.getTotalStudentCount();
                totalStudents = studentCount;
            } else {
                recentStudents = studentService.getStudentsByUser(currentUser);
                // Limit to 5 recent students for display
                if (recentStudents.size() > 5) {
                    recentStudents = recentStudents.subList(0, 5);
                }
                studentCount = studentService.getStudentCountByUser(currentUser.getId());
            }

            model.addAttribute("recentStudents", recentStudents);
            model.addAttribute("studentCount", studentCount);
            model.addAttribute("totalStudents", totalStudents);
            model.addAttribute("currentUser", currentUser);
        }

        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}