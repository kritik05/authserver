package com.authserver.Authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Use the Apache HttpComponents library to enable PATCH
        HttpComponentsClientHttpRequestFactory requestFactory
                = new HttpComponentsClientHttpRequestFactory();

        // Optionally, you can tune timeouts:
        // requestFactory.setConnectTimeout(5000);
        // requestFactory.setReadTimeout(10000);

        return new RestTemplate(requestFactory);
    }
}