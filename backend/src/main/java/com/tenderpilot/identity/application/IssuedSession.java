package com.tenderpilot.identity.application;

import com.tenderpilot.identity.api.AuthSessionResponse;

public record IssuedSession(

        AuthSessionResponse response,

        String refreshToken

) {
}
