package com.studentmanagement.controller;

import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserByEmail(email).orElse(null);
    }

    @GetMapping("/list")
    public String listStudents(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        User currentUser = getCurrentUser();
        List<Student> students;

        if (keyword != null && !keyword.trim().isEmpty()) {
            students = studentService.searchStudents(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            if (currentUser.getRole().equals("ADMIN")) {
                students = studentService.getAllStudents();
            } else {
                students = studentService.getStudentsByUser(currentUser);
            }
        }

        model.addAttribute("students", students);
        model.addAttribute("currentUser", currentUser);
        return "student/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        return "student/form";
    }

    @PostMapping("/add")
    public String addStudent(@Valid @ModelAttribute("student") Student student,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            return "student/form";
        }

        // Check if email already exists (only if email is provided)
        if (student.getEmail() != null && !student.getEmail().isEmpty() &&
                studentService.emailExists(student.getEmail())) {
            result.rejectValue("email", "error.student", "Email already exists");
            return "student/form";
        }

        User currentUser = getCurrentUser();
        try {
            studentService.createStudent(student, currentUser);
            return "redirect:/student/list?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating student: " + e.getMessage());
            return "student/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Optional<Student> student = studentService.getStudentById(id);
        User currentUser = getCurrentUser();

        if (student.isPresent()) {
            // Check if user has permission to edit this student
            if (!currentUser.getRole().equals("ADMIN") &&
                    !student.get().getUser().getId().equals(currentUser.getId())) {
                return "redirect:/access-denied";
            }

            model.addAttribute("student", student.get());
            return "student/form";
        }

        return "redirect:/student/list?error";
    }

    @PostMapping("/edit/{id}")
    public String updateStudent(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("student") Student student,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "student/form";
        }

        User currentUser = getCurrentUser();
        Optional<Student> existingStudent = studentService.getStudentById(id);

        if (existingStudent.isPresent()) {
            // Check if user has permission to edit this student
            if (!currentUser.getRole().equals("ADMIN") &&
                    !existingStudent.get().getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied!");
                return "redirect:/access-denied";
            }

            // Check if email is changed and already exists
            if (student.getEmail() != null && !student.getEmail().isEmpty() &&
                    !existingStudent.get().getEmail().equals(student.getEmail()) &&
                    studentService.emailExists(student.getEmail())) {
                result.rejectValue("email", "error.student", "Email already exists");
                return "student/form";
            }

            try {
                studentService.updateStudent(id, student);
                redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
                return "redirect:/student/list?success";
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Error updating student: " + e.getMessage());
                return "student/form";
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Student not found!");
        return "redirect:/student/list?error";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        Optional<Student> student = studentService.getStudentById(id);

        if (student.isPresent()) {
            // Check if user has permission to delete this student
            if (!currentUser.getRole().equals("ADMIN") &&
                    !student.get().getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied!");
                return "redirect:/access-denied";
            }

            try {
                studentService.deleteStudent(id);
                redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully!");
                return "redirect:/student/list?success";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error deleting student: " + e.getMessage());
                return "redirect:/student/list?error";
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Student not found!");
        return "redirect:/student/list?error";
    }

    // View method has been removed as requested
}