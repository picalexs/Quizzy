// com/backend/controller/StreakControllerTest.java
package com.backend.controller;

import com.backend.config.StreakTestConfig;
import com.backend.config.SecurityConfig;
import com.backend.model.Streak;
import com.backend.service.StreakService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StreakController.class)
@Import({StreakTestConfig.class, SecurityConfig.class})
class StreakControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StreakService streakService;

    @Autowired
    private ObjectMapper objectMapper;

    private Streak streak;

    @BeforeEach
    void setUp() {
        streak = new Streak();
        streak.setId(1L);
        streak.setCurrentStreak(10);
        streak.setLastCompletedDate(Date.valueOf("2024-04-25"));
        streak.setUser(null); // Pentru test simplificat
    }

    @Test
    void testGetAllStreaks() throws Exception {
        Mockito.when(streakService.getAllStreaks()).thenReturn(List.of(streak));

        mockMvc.perform(get("/api/streaks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(streak.getId()));
    }

    @Test
    void testGetStreakById() throws Exception {
        Mockito.when(streakService.getStreakById(1L)).thenReturn(java.util.Optional.of(streak));

        mockMvc.perform(get("/api/streaks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(10));
    }

    @Test
    void testGetStreaksByUserId() throws Exception {
        Mockito.when(streakService.getStreaksByUserId(1)).thenReturn(List.of(streak));

        mockMvc.perform(get("/api/streaks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(streak.getId()));
    }

    @Test
    void testGetStreakByUserIdAndDate() throws Exception {
        Mockito.when(streakService.getStreakByUserIdAndDate(anyInt(), any(Date.class)))
                .thenReturn(java.util.Optional.of(streak));

        mockMvc.perform(get("/api/streaks/user/1/date/2024-04-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(streak.getId()));
    }

    @Test
    void testGetStreaksByMinimumStreak() throws Exception {
        Mockito.when(streakService.getStreaksByMinimumStreak(5)).thenReturn(List.of(streak));

        mockMvc.perform(get("/api/streaks/min/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentStreak").value(10));
    }

    @Test
    void testGetTopStreaks() throws Exception {
        Mockito.when(streakService.getTopStreaks()).thenReturn(List.of(streak));

        mockMvc.perform(get("/api/streaks/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(streak.getId()));
    }

    @Test
    void testGetAverageStreak() throws Exception {
        Mockito.when(streakService.getAverageStreak()).thenReturn(7.0f);

        mockMvc.perform(get("/api/streaks/average"))
                .andExpect(status().isOk())
                .andExpect(content().string("7.0"));
    }

    @Test
    void testCreateStreak() throws Exception {
        Mockito.when(streakService.createStreak(any(Streak.class))).thenReturn(streak);

        mockMvc.perform(post("/api/streaks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(streak)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(streak.getId()))
                .andExpect(jsonPath("$.currentStreak").value(10));
    }

    @Test
    void testDeleteStreak() throws Exception {
        mockMvc.perform(delete("/api/streaks/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(streakService).deleteStreak(1L);
    }
}
