package com.backend.config;

import com.backend.service.FlashcardService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FlashcardTestConfig {

    @Bean
    public FlashcardService flashcardService() {
        return Mockito.mock(FlashcardService.class);
    }
}
