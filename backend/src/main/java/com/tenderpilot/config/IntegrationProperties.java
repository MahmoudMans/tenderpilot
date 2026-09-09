package com.tenderpilot.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "tenderpilot.integrations"
)
public record IntegrationProperties(

        Endpoint opensearch,

        Endpoint ollama

) {

    public record Endpoint(

            URI baseUrl

    ) {
    }

}
