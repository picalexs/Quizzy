package com.backend.repository;

import com.backend.model.Course;
import com.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.Collection;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    @Query("SELECT c FROM Course c")
    Collection<Course> allCourses();

//    @Modifying
//    @Transactional
//    @Query(value = "INSERT INTO enrollment (user_id, course_id, enrollment_date, grade) VALUES (:userId, :courseId, CURRENT_DATE, NULL)", nativeQuery = true)
//    void setCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
