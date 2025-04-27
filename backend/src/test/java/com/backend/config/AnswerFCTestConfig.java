package com.backend.config;

import com.backend.service.AnswerFCService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AnswerFCTestConfig {

    @Bean
    public AnswerFCService answerFCService() {
        return Mockito.mock(AnswerFCService.class);
    }
}
