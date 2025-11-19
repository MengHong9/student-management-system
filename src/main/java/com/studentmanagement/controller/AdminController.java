package com.studentmanagement.controller;

import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        long totalUsers = userService.getUserCount();
        long totalStudents = studentService.getTotalStudentCount();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalStudents", totalStudents);

        return "admin/dashboard";
    }
}