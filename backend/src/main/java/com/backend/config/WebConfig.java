package com.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.util.AntPathMatcher;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Use PathPatternParser for trailing slash match and case-insensitive matching
        org.springframework.web.util.pattern.PathPatternParser patternParser = new org.springframework.web.util.pattern.PathPatternParser();
        patternParser.setMatchOptionalTrailingSeparator(true);
        patternParser.setCaseSensitive(false);
        configurer.setPatternParser(patternParser);
    }
}
