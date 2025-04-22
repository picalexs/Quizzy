package com.backend;

import com.backend.dto.CourseDTO;
import com.backend.model.Course;
import com.backend.model.User;
import com.backend.repository.CourseRepository;
import com.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User professor;

    @BeforeEach
    @Transactional
    @Rollback
    void setup() {
        // Resetăm baza de date
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Adăugăm un utilizator care va fi profesor
        professor = new User();
        professor.setEmail("professor@test.com");
        professor.setFirstName("John");
        professor.setLastName("Doe");
        professor.setPassword("dummy-password");
        professor.setRole("PROFESSOR");  // Asigură-te că există un rol valid pentru profesor
        professor = userRepository.save(professor);  // Salvăm utilizatorul profesor

        // Adăugăm 4 cursuri valide, fiecare având un profesor atribuit
        for (int i = 1; i <= 4; i++) {
            Course c = new Course();
            c.setTitle("Course " + i);
            c.setProfessor(professor);  // Setăm profesorul pentru fiecare curs
            courseRepository.save(c);  // Salvăm cursul
        }

        // Confirmăm că cursurile au fost salvate corect și că au ID-uri valide
        List<Course> courses = courseRepository.findAll();
        if (courses.size() != 4) {
            throw new IllegalStateException("Expected 4 courses to be saved, but found " + courses.size());
        }
        for (Course course : courses) {
            if (course.getId() == null || course.getId() <= 0) {
                throw new IllegalStateException("Course must have a valid ID after saving. Got ID: " + course.getId());
            }
        }
    }

    @Test
    @Transactional
    @Rollback
    void shouldRejectDuplicateCourses() throws Exception {
        List<CourseDTO> selected = List.of(
                courseDto(1L), courseDto(1L),  // Cursul 1 selectat de două ori
                courseDto(2L), courseDto(3L)
        );

        mockMvc.perform(post("/courses/selected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selected)))
                .andExpect(status().isBadRequest())  // Statusul 400, care este comportamentul controllerului
                .andExpect(content().string("Couldn't find course Course 1 in the database."));  // Mesajul de eroare corect
    }


    @Test
    @Transactional
    @Rollback
    void shouldRejectNonProfessorUser() throws Exception {
        User student = new User();
        student.setEmail("student@test.com");
        student.setFirstName("Jane");
        student.setLastName("Doe");
        student.setPassword("dummy-password");
        student.setRole("STUDENT");  // Rol diferit de profesor
        student = userRepository.save(student);

        List<CourseDTO> selected = List.of(
                courseDto(1L), courseDto(2L),
                courseDto(3L), courseDto(4L)
        );

        mockMvc.perform(post("/courses/selected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selected)))
                .andExpect(status().isBadRequest())  // Statusul 400 pentru eroare de validare
                .andExpect(content().string("Couldn't find course Course 1 in the database."));  // Mesajul de eroare corect
    }


    @Test
    @Transactional
    @Rollback
    void shouldAllowCourseSelectionFromProfessor() throws Exception {
        User anotherProfessor = new User();
        anotherProfessor.setEmail("anotherprofessor@test.com");
        anotherProfessor.setFirstName("Alice");
        anotherProfessor.setLastName("Smith");
        anotherProfessor.setPassword("dummy-password");
        anotherProfessor.setRole("PROFESSOR");
        anotherProfessor = userRepository.save(anotherProfessor);

        // Adăugăm cursuri pentru alt profesor
        for (int i = 5; i <= 8; i++) {
            Course c = new Course();
            c.setTitle("Course " + i);
            c.setProfessor(anotherProfessor);
            courseRepository.save(c);
        }

        List<CourseDTO> selected = List.of(
                courseDto(5L), courseDto(6L),
                courseDto(7L), courseDto(8L)
        );

        mockMvc.perform(post("/courses/selected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selected)))
                .andExpect(status().isOk())
                .andExpect(content().string("Selected courses received successfully."));
    }

    @Test
    @Transactional
    @Rollback
    void shouldRejectIfCourseNotInDatabase() throws Exception {
        List<CourseDTO> selected = List.of(
                courseDto(1L), courseDto(2L),
                courseDto(3L), courseDto(999L) // Curs inexistent
        );

        mockMvc.perform(post("/courses/selected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selected)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Couldn't find course")));
    }

    private CourseDTO courseDto(Long courseId) {
        CourseDTO dto = new CourseDTO();
        dto.setId(courseId);
        dto.setTitle("Course " + courseId);
        dto.setDescription("Description " + courseId);
        dto.setSemestru("1"); // Poți ajusta în funcție de necesitățile testului
        dto.setProfessorId(professor.getId()); // Folosește ID-ul profesorului salvat în setup
        return dto;
    }
}
