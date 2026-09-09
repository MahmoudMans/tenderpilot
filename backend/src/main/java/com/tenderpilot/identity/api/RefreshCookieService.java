package com.tenderpilot.identity.api;

import com.tenderpilot.security.SecurityProperties;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieService {

    private final SecurityProperties
            securityProperties;


    public RefreshCookieService(
            SecurityProperties securityProperties
    ) {

        this.securityProperties =
                securityProperties;

    }


    public ResponseCookie create(
            String rawRefreshToken
    ) {

        return ResponseCookie

                .from(
                        securityProperties
                                .refreshCookieName(),
                        rawRefreshToken
                )

                .httpOnly(
                        true
                )

                .secure(
                        securityProperties
                                .refreshCookieSecure()
                )

                .sameSite(
                        "Strict"
                )

                .path(
                        "/api/v1/auth"
                )

                .maxAge(
                        securityProperties
                                .refreshTokenTtl()
                )

                .build();

    }


    public ResponseCookie clear() {

        return ResponseCookie

                .from(
                        securityProperties
                                .refreshCookieName(),
                        ""
                )

                .httpOnly(
                        true
                )

                .secure(
                        securityProperties
                                .refreshCookieSecure()
                )

                .sameSite(
                        "Strict"
                )

                .path(
                        "/api/v1/auth"
                )

                .maxAge(
                        Duration.ZERO
                )

                .build();

    }

}
