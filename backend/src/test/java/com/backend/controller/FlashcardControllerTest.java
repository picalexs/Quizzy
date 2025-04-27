package com.backend.controller;

import com.backend.config.SecurityConfig;
import com.backend.config.FlashcardTestConfig;
import com.backend.model.Flashcard;
import com.backend.model.Material;
import com.backend.model.User;
import com.backend.service.FlashcardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlashcardController.class)
@Import({SecurityConfig.class, FlashcardTestConfig.class})
public class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FlashcardService flashcardService;

    private Flashcard flashcard;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setPassword("pass");

        Material material = new Material();
        material.setId(1L);
        material.setName("Test Material");
        material.setPath("Example content");

        flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setQuestion("What is Java?");
        flashcard.setLevel(1);
        flashcard.setLastStudiedAt(new Date());
        flashcard.setQuestionType("single");
        flashcard.setUser(user);
        flashcard.setMaterial(material);
    }

    @Test
    void testGetAllFlashcards() throws Exception {
        when(flashcardService.getAllFlashcards()).thenReturn(List.of(flashcard));

        mockMvc.perform(get("/api/flashcards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(flashcard.getId()))
                .andExpect(jsonPath("$[0].question").value("What is Java?"));
    }

    @Test
    void testGetFlashcardById() throws Exception {
        when(flashcardService.getFlashcardById(1L)).thenReturn(Optional.ofNullable(flashcard));

        mockMvc.perform(get("/api/flashcards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("What is Java?"));
    }

    @Test
    void testCreateFlashcard() throws Exception {
        when(flashcardService.createFlashcard(any(Flashcard.class))).thenReturn(flashcard);

        mockMvc.perform(post("/api/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(flashcard.getId()))
                .andExpect(jsonPath("$.question").value("What is Java?"));
    }

    @Test
    void testDeleteFlashcard() throws Exception {
        Mockito.doNothing().when(flashcardService).deleteFlashcard(1L);

        mockMvc.perform(delete("/api/flashcards/1"))
                .andExpect(status().isOk());
    }
}
