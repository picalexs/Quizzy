package com.backend.service;

import com.backend.dto.CourseDTO;
import com.backend.mapper.CourseMapper;
import com.backend.model.Course;
import com.backend.repository.CourseRepository;
import com.backend.repository.UserRepository;
import com.backend.repository.FlashcardRepository;
import com.backend.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FlashcardRepository flashcardRepository;
    private final MaterialRepository materialRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, UserRepository userRepository, FlashcardRepository flashcardRepository, MaterialRepository materialRepository) {
            this.courseRepository = courseRepository;
            this.userRepository = userRepository;
            this.flashcardRepository = flashcardRepository;
            this.materialRepository = materialRepository;
        }

    public Course createCourse(CourseDTO courseDTO) {

        courseDTO.setId(null);

        CourseMapper mapper = new CourseMapper(userRepository);
        Course course = mapper.toEntity(courseDTO);
        return courseRepository.save(course);
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

        Optional<Course> existingCourseOpt = courseRepository.findById(id);
        if (existingCourseOpt.isEmpty()) {
            return Optional.empty();
        }

        Course existingCourse = existingCourseOpt.get();

        existingCourse.setTitle(courseDTO.getTitle());
        existingCourse.setDescription(courseDTO.getDescription());
        existingCourse.setSemestru(courseDTO.getSemestru());

        Integer professorId = courseDTO.getProfessorId();
        if (professorId != null) {
            userRepository.findById(professorId).ifPresent(existingCourse::setProfessor);
        }

        Course updatedCourse = courseRepository.save(existingCourse);

        return Optional.of(updatedCourse);
    }

    public boolean deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            return false;
        }
        courseRepository.deleteById(id);
        return true;
    }

    public Course findByTitle(String Title) {
        return courseRepository.findByTitle(Title);
    }

    public Optional<Course> getCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    public List<Course> getEnrolledCoursesByStudentId(Integer studentId) {
        return courseRepository.findEnrolledCoursesByStudentId(studentId);
    }

    public List<Course> getCoursesByProfessorId(Integer professorId) {
        return courseRepository.findByProfessorId(professorId);
    }


    /**
     * Get flashcard counts for multiple courses in batch to avoid N+1 query problem
     */
    public Map<Long, Long> getFlashcardCountsByCourseIds(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return flashcardRepository.getFlashcardCountsByCourseIds(courseIds);
    }

    /**
     * Get material counts for multiple courses in batch to avoid N+1 query problem
     */
    public Map<Long, Long> getMaterialCountsByCourseIds(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return materialRepository.getMaterialCountsByCourseIds(courseIds);
    }
}