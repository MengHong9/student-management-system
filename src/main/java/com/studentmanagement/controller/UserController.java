package com.studentmanagement.controller;

import com.studentmanagement.entity.User;
import com.studentmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/users/form";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("user") User user,
                          BindingResult result,
                          Model model) {
        if (result.hasErrors()) {
            return "admin/users/form";
        }

        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
            return "admin/users/form";
        }

        try {
            userService.createUser(user);
            return "redirect:/admin/users?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating user: " + e.getMessage());
            return "admin/users/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/users/form";
        }
        return "redirect:/admin/users?error";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/users/form";
        }

        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isPresent()) {
            // Check if email is changed and already exists
            if (!existingUser.get().getEmail().equals(user.getEmail()) &&
                    userService.emailExists(user.getEmail())) {
                result.rejectValue("email", "error.user", "Email already exists");
                return "admin/users/form";
            }

            try {
                userService.updateUser(id, user);
                redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
                return "redirect:/admin/users?success";
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Error updating user: " + e.getMessage());
                return "admin/users/form";
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
        return "redirect:/admin/users?error";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
            return "redirect:/admin/users?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
            return "redirect:/admin/users?error";
        }
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setEnabled(!existingUser.isEnabled());
            try {
                userService.updateUser(id, existingUser);
                String status = existingUser.isEnabled() ? "enabled" : "disabled";
                redirectAttributes.addFlashAttribute("successMessage", "User " + status + " successfully!");
                return "redirect:/admin/users?success";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error updating user status: " + e.getMessage());
                return "redirect:/admin/users?error";
            }
        }
        redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
        return "redirect:/admin/users?error";
    }
}