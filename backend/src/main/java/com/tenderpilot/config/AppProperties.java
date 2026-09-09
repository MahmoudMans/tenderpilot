package com.tenderpilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "tenderpilot.app"
)
public record AppProperties(

        String displayName,

        String version

) {
}
