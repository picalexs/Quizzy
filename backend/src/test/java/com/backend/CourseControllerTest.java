package com.backend;

import com.backend.controller.CourseController;
import com.backend.dto.CourseDTO;
import com.backend.model.Course;
import com.backend.service.CourseService;
import com.backend.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.MockitoAnnotations.openMocks;

class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private CourseController courseController;

    public CourseControllerTest() {
        openMocks(this);
    }

    private CourseDTO createCourse(Long id, String title) {
        CourseDTO course = new CourseDTO();
        course.setId(id);
        course.setTitle(title);
        return course;
    }

    @Test
    void shouldReturnAllCourses() {
        List<Course> courses = List.of(new Course(), new Course());
        when(courseService.getAllCourses()).thenReturn(courses);

        ResponseEntity<Collection<Course>> response = courseController.getAllUsers();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void shouldReturnCourseByIdIfExists() {
        Course course = new Course();
        course.setId(1L);
        when(courseService.findById(1L)).thenReturn(Optional.of(course));

        ResponseEntity<Course> response = courseController.getCourseById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void shouldReturnNotFoundIfCourseDoesNotExist() {
        when(courseService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Course> response = courseController.getCourseById(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldRejectMoreThanFourCourses() {
        List<CourseDTO> courses = Arrays.asList(
                createCourse(1L, "A"), createCourse(2L, "B"),
                createCourse(3L, "C"), createCourse(4L, "D"),
                createCourse(5L, "E")
        );

        ResponseEntity<String> response = courseController.receiveSelectedCourses(courses);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void shouldRejectEmptySelection() {
        List<CourseDTO> courses = new ArrayList<>();
        ResponseEntity<String> response = courseController.receiveSelectedCourses(courses);
        assertEquals(400, response.getStatusCodeValue());
    }


    @Test
    void shouldRejectInvalidCourse() {
        List<CourseDTO> courses = Arrays.asList(
                createCourse(1L, "A"),
                createCourse(2L, "B"),
                createCourse(3L, "Invalid"), // acesta va fi invalid
                createCourse(4L, "D")
        );

        when(courseService.checkCourseById(1L)).thenReturn(true);
        when(courseService.checkCourseById(2L)).thenReturn(true);
        when(courseService.checkCourseById(3L)).thenReturn(false); // curs invalid
        when(courseService.checkCourseById(4L)).thenReturn(true);

        ResponseEntity<String> response = courseController.receiveSelectedCourses(courses);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Couldn't find course"));
    }


    @Test
    void shouldAcceptValidCourses() {
        List<CourseDTO> courses = Arrays.asList(
                createCourse(1L, "A"), createCourse(2L, "B"),
                createCourse(3L, "C"), createCourse(4L, "D")
        );

        when(courseService.checkCourseById(anyLong())).thenReturn(true);

        ResponseEntity<String> response = courseController.receiveSelectedCourses(courses);
        assertEquals(200, response.getStatusCodeValue());
        verify(enrollmentService, times(4)).addEnrollment(eq(1), anyLong());
    }
}
