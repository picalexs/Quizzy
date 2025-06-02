package com.backend.controller;

import com.backend.dto.CourseDTO;
import com.backend.mapper.CourseMapper;
import com.backend.model.Course;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import com.backend.repository.FlashcardRepository;
import com.backend.service.CourseService;
import com.backend.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/courses")
public class CourseController {


    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final FlashcardRepository flashcardRepository;

    @Autowired
    public CourseController(CourseService courseService, EnrollmentService enrollmentService, FlashcardRepository flashcardRepository) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.flashcardRepository = flashcardRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCourses() {
        List<Course> courses = (List<Course>) courseService.getAllCourses();
        
        // Get all course IDs for batch flashcard count query
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, Long> flashcardCounts = courseService.getFlashcardCountsByCourseIds(courseIds);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Course c : courses) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("title", c.getTitle());
            map.put("description", c.getDescription());
            map.put("semestru", c.getSemestru());
            map.put("flashcardCount", flashcardCounts.getOrDefault(c.getId(), 0L));
            // Remove materials to avoid lazy loading - they can be fetched separately when needed
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.createCourse(courseDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        return courseService.updateCourse(id, courseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        boolean deleted = courseService.deleteCourse(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/selected")
    public ResponseEntity<String> receiveSelectedCourses(@RequestBody List<CourseDTO> selectedCourses) {

        if (selectedCourses.size() != 4) {
            return ResponseEntity.badRequest().body("The user should select exactly four courses !");
        }

        for (CourseDTO course : selectedCourses) {
            if (!courseService.checkCourseById(course.getId())) {
                return ResponseEntity.badRequest().body("Couldn't find course " + course.getTitle() + " in the database.");
            }
        }

        for (CourseDTO course : selectedCourses) {
            enrollmentService.addEnrollment(1, course.getId());
        }
        return ResponseEntity.ok("Selected courses received successfully.");
    }

}