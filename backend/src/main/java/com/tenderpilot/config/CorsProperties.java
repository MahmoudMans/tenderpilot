package com.tenderpilot.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "tenderpilot.cors"
)
public record CorsProperties(

        List<String> allowedOrigins

) {

    public CorsProperties {

        allowedOrigins =
                allowedOrigins == null ||
                allowedOrigins.isEmpty()

                        ? List.of(
                                "http://localhost:3000"
                        )

                        : List.copyOf(
                                allowedOrigins
                        );

    }

}
