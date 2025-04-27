package com.backend.config;

import com.backend.service.StreakService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class StreakTestConfig {

    @Bean
    public StreakService streakService() {
        return Mockito.mock(StreakService.class);
    }
}
