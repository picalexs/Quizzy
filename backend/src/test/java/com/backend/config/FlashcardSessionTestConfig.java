package com.backend.config;

import com.backend.service.FlashcardSessionService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FlashcardSessionTestConfig {

    @Bean
    public FlashcardSessionService flashcardSessionService() {
        return Mockito.mock(FlashcardSessionService.class);
    }
}