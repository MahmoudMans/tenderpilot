package com.tenderpilot.identity.application;

import java.time.Instant;

public record IssuedAccessToken(

        String value,

        Instant expiresAt,

        long expiresInSeconds

) {
}
