package com.studentmanagement.service;

import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> getStudentsByUser(User user) {
        return studentRepository.findByUser(user);
    }

    public List<Student> getStudentsByUserId(Long userId) {
        return studentRepository.findByUserId(userId);
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Student createStudent(Student student, User user) {
        student.setUser(user);
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student studentDetails) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            student.setFirstName(studentDetails.getFirstName());
            student.setLastName(studentDetails.getLastName());
            student.setEmail(studentDetails.getEmail());
            student.setPhone(studentDetails.getPhone());
            student.setAddress(studentDetails.getAddress());
            student.setDateOfBirth(studentDetails.getDateOfBirth());
            student.setGender(studentDetails.getGender());
            student.setCourse(studentDetails.getCourse());
            student.setDepartment(studentDetails.getDepartment());

            return studentRepository.save(student);
        }
        throw new RuntimeException("Student not found with id: " + id);
    }

    public void deleteStudent(Long id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Student not found with id: " + id);
        }
    }

    public boolean emailExists(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return studentRepository.existsByEmail(email);
    }

    public List<Student> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentRepository.searchStudents(keyword.trim());
    }

    public long getTotalStudentCount() {
        return studentRepository.countAllStudents();
    }

    public long getStudentCountByUser(Long userId) {
        return studentRepository.countStudentsByUserId(userId);
    }
}