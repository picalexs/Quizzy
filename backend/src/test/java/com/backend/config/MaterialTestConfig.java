// com/backend/config/MaterialTestConfig.java
package com.backend.config;

import com.backend.service.MaterialService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MaterialTestConfig {

    @Bean
    public MaterialService materialService() {
        return Mockito.mock(MaterialService.class);
    }
}
