package com.backend.controller;


import com.backend.config.SecurityConfig;
import com.backend.config.FlashcardSessionTestConfig;
import com.backend.model.Course;
import com.backend.model.FlashcardSession;
import com.backend.model.User;
import com.backend.service.FlashcardSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlashcardSessionController.class)
@Import({FlashcardSessionTestConfig.class, SecurityConfig.class})
public class FlashcardSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlashcardSessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    private FlashcardSession session;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("secret");
        user.setRole("student");

        Course course = new Course();
        course.setId(2L);
        course.setTitle("Java 101");
        course.setDescription("Intro course");
        course.setSemestru("Spring");
        course.setProfessor(user);

        session = new FlashcardSession();
        session.setId(1L);
        session.setTimestamp(new Date());
        session.setFlashcardCount(10);
        session.setUser(user);
        session.setCourse(course);
    }


    @Test
    void testGetAllSessions() throws Exception {
        List<FlashcardSession> sessions = Arrays.asList(session);

        Mockito.when(sessionService.getAllSessions()).thenReturn(sessions);

        mockMvc.perform(get("/flashcardsessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(session.getId()));
    }

    @Test
    void testGetSessionsByUserId() throws Exception {
        Mockito.when(sessionService.getSessionsByUserId(1)).thenReturn(List.of(session));

        mockMvc.perform(get("/flashcardsessions/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flashcardCount").value(session.getFlashcardCount()));
    }

    @Test
    void testCreateSession() throws Exception {
        Mockito.when(sessionService.createSession(any(FlashcardSession.class))).thenReturn(session);

        mockMvc.perform(post("/flashcardsessions")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(session)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(session.getId()))
                .andExpect(jsonPath("$.flashcardCount").value(session.getFlashcardCount()));
    }
}
