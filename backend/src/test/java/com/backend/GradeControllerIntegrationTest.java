package com.backend;

import com.backend.dto.CourseDTO;
import com.backend.model.*;
import com.backend.repository.CourseRepository;
import com.backend.repository.GradeRepository;
import com.backend.repository.TestRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class GradeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private GradeRepository gradeRepository;

    private User student;
    private TestEntity test;
    private Grade grade;

    @BeforeEach
    @Transactional
    @Rollback
    void setup() {
        student = new User();
        student.setFirstName("Student1");
        student.setLastName("Doe");
        student.setEmail("student1@example.com");
        student.setRole("STUDENT");
        student.setPassword("securePassword123");
        userRepository.save(student);

        test = new TestEntity();
        test.setTitle("Math Test");
        testRepository.save(test);

        grade = new Grade();
        grade.setId(new GradeId(test.getId(), student.getId()));
        grade.setGrade(85.5f);
        grade.setSubmissionDate(new Date());
        gradeRepository.save(grade);
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetAllGrades() throws Exception {
        mockMvc.perform(get("/grades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].grade").value(85.5f));
    }

    @Test
    @Transactional
    @Rollback
    void shouldCreateGrade() throws Exception {
        Grade newGrade = new Grade();
        newGrade.setId(new GradeId(test.getId(), student.getId()));
        newGrade.setGrade(90.0f);
        newGrade.setSubmissionDate(new Date());

        mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGrade)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.grade").value(90.0f));
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetGradeById() throws Exception {
        mockMvc.perform(get("/grades/" + test.getId() + "/" + student.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value(grade.getGrade()));
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateGrade() throws Exception {
        grade.setGrade(95.0f);

        mockMvc.perform(put("/grades/" + test.getId() + "/" + student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grade)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value(95.0f));
    }

    @Test
    @Transactional
    @Rollback
    void shouldDeleteGrade() throws Exception {
        mockMvc.perform(delete("/grades/" + test.getId() + "/" + student.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/grades/" + test.getId() + "/" + student.getId()))
                .andExpect(status().isNotFound());
    }
}