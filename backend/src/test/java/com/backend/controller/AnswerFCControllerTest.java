package com.backend.controller;

import com.backend.config.AnswerFCTestConfig;
import com.backend.config.SecurityConfig;
import com.backend.model.AnswerFC;
import com.backend.model.Flashcard;
import com.backend.service.AnswerFCService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnswerFCController.class)
@Import({AnswerFCTestConfig.class, SecurityConfig.class})
class AnswerFCControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnswerFCService answerFCService;

    @Autowired
    private ObjectMapper objectMapper;

    private AnswerFC answer;

    @BeforeEach
    void setUp() {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(100L);

        answer = new AnswerFC();
        answer.setId(1L);
        answer.setOptionText("Option A");
        answer.setCorrect(true);
        answer.setFlashcard(flashcard);
    }

    @Test
    void testGetAllAnswers() throws Exception {
        Mockito.when(answerFCService.getAllAnswers()).thenReturn(List.of(answer));

        mockMvc.perform(get("/api/answers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(answer.getId()))
                .andExpect(jsonPath("$[0].optionText").value("Option A"));
    }

    @Test
    void testGetAnswerById() throws Exception {
        Mockito.when(answerFCService.getAnswerById(1L)).thenReturn(answer);

        mockMvc.perform(get("/api/answers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionText").value("Option A"));
    }

    @Test
    void testCreateAnswer() throws Exception {
        Mockito.when(answerFCService.createAnswer(any(AnswerFC.class))).thenReturn(answer);

        mockMvc.perform(post("/api/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(answer.getId()))
                .andExpect(jsonPath("$.optionText").value("Option A"));
    }

    @Test
    void testUpdateAnswer() throws Exception {
        AnswerFC updated = new AnswerFC();
        updated.setId(1L);
        updated.setOptionText("Updated Option");
        updated.setCorrect(false);
        updated.setFlashcard(answer.getFlashcard());

        Mockito.when(answerFCService.updateAnswer(anyLong(), any(AnswerFC.class))).thenReturn(updated);

        mockMvc.perform(put("/api/answers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionText").value("Updated Option"))
                .andExpect(jsonPath("$.correct").value(false));
    }

    @Test
    void testDeleteAnswer() throws Exception {
        mockMvc.perform(delete("/api/answers/1"))
                .andExpect(status().isNoContent());
        Mockito.verify(answerFCService).deleteAnswer(1L);
    }
}
