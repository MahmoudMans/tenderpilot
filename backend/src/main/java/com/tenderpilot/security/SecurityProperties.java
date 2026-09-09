package com.tenderpilot.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "tenderpilot.security"
)
public record SecurityProperties(

        String issuer,

        String accessTokenSecret,

        Duration accessTokenTtl,

        Duration refreshTokenTtl,

        String refreshCookieName,

        boolean refreshCookieSecure

) {
}
