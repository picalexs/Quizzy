package com.backend.service;

import com.backend.dto.CourseDTO;
import com.backend.mapper.CourseMapper;
import com.backend.model.Course;
import com.backend.model.User;
import com.backend.repository.CourseRepository;
import com.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
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

    public Optional<Course> updateCourse(Long id, CourseDTO courseDTO) {
        CourseMapper mapper = new CourseMapper(userRepository);
        Course newCourse = mapper.toEntity(courseDTO);
        if(!courseRepository.existsById(id)) {
            return Optional.empty();
        }
        return Optional.of(newCourse);
    }

    public boolean deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            return false;
        }
        courseRepository.deleteById(id);
        return true;
    }
}