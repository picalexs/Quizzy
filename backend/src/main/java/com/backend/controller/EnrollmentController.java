package com.backend.controller;

import com.backend.model.Course;
import com.backend.model.Enrollment;
import com.backend.repository.CourseRepository;
import com.backend.service.CourseService;
import com.backend.service.EnrollmentService;
import com.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService,UserService userService, CourseService courseService) { this.enrollmentService = enrollmentService; this.userService = userService; this.courseService = courseService; }

    @GetMapping
    public ResponseEntity<Collection<Enrollment>> getAllEnrollments(){
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @GetMapping("my_selection")
    public ResponseEntity<Collection<Course>> getSelected() {
        Integer currentId = 1;

        Collection<Enrollment> foundEnrollments = enrollmentService.getALLEnrollmentsByUserId(currentId);
        if (foundEnrollments.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        List<Course> list = new ArrayList<>();

        for( Enrollment enrollment : foundEnrollments ) {
            Optional<Course> course = courseService.findById(enrollment.getId().getCourseID());
            course.ifPresent(list::add);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/user_{id}")
    public ResponseEntity<Collection<Enrollment>> getEnrollmentsByUserId(@PathVariable Integer id) {
        if(!userService.checkUserById(id)){
            return ResponseEntity.notFound().build();
        }
        Collection<Enrollment> foundEnrollments = enrollmentService.getALLEnrollmentsByUserId(id);
        if (foundEnrollments.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(foundEnrollments);
    }

}
