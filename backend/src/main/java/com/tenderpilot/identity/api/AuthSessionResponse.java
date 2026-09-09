package com.tenderpilot.identity.api;

public record AuthSessionResponse(

        String accessToken,

        long expiresInSeconds,

        AuthUserResponse user

) {
}
