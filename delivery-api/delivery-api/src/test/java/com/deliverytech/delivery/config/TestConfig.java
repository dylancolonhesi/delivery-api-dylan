package com.deliverytech.delivery.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class TestConfig {
    
    @Bean
    @Primary
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
