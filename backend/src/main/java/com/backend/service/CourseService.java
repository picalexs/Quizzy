package com.backend.service;

import com.backend.dto.CourseDTO;
import com.backend.model.Course;
import com.backend.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Collection<Course> getAllCourses() {
        return courseRepository.allCourses();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public boolean checkCourseById(Long id) {
        return courseRepository.existsById(id);
    }

}