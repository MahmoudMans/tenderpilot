package com.tenderpilot.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    @Bean
    HttpClient integrationHttpClient() {

        return HttpClient
                .newBuilder()
                .connectTimeout(
                        Duration.ofSeconds(2)
                )
                .build();

    }

}
