package com.polarbookshop.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    WebClient webClient(
            ClientProperties clientProperties,
            WebClient.Builder webClientBuilder // WebClient 빈을 만들기 위해 스프링 부트가 자동 설정한 객체
    ) {
        return webClientBuilder
                .baseUrl(clientProperties.catalogServiceUri().toString())
                .build();
    }
}
