package com.programthis.review_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration // Indica que esta clase es una fuente de definición de beans
public class RestTemplateConfig {

    @Bean // Marca este método como un productor de un bean que Spring gestionará
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}