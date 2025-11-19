package com.studentmanagement.repository;

import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByUser(User user);
    boolean existsByEmail(String email);

    @Query("SELECT s FROM Student s WHERE s.user.id = :userId")
    List<Student> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Student s")
    long countAllStudents();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.user.id = :userId")
    long countStudentsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Student> searchStudents(@Param("keyword") String keyword);
}