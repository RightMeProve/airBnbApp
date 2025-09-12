package com.rightmeprove.airbnb.airBnbApp.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Marks this class as a source of Spring bean definitions
public class MapperConfig {

    /**
     * Registers a ModelMapper bean in the Spring context.
     *
     * Why is this needed?
     * - Spring Boot does not provide ModelMapper out-of-the-box.
     * - By defining this @Bean, we tell Spring how to create and manage a single instance of ModelMapper.
     * - Once registered, ModelMapper can be injected anywhere in the app using @Autowired.
     * - Central place to add custom mapping configurations if needed.
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}

