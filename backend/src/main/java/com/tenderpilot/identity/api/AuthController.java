package com.tenderpilot.identity.api;

import com.tenderpilot.common.domain.ApiAuthenticationException;

import com.tenderpilot.identity.application.AuthenticationService;
import com.tenderpilot.identity.application.CurrentSessionService;
import com.tenderpilot.identity.application.IssuedSession;

import com.tenderpilot.security.SecurityProperties;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/auth"
)
public class AuthController {

    private final AuthenticationService
            authenticationService;


    private final CurrentSessionService
            currentSessionService;


    private final RefreshCookieService
            refreshCookieService;


    private final SecurityProperties
            securityProperties;


    public AuthController(

            AuthenticationService authenticationService,

            CurrentSessionService currentSessionService,

            RefreshCookieService refreshCookieService,

            SecurityProperties securityProperties

    ) {

        this.authenticationService =
                authenticationService;

        this.currentSessionService =
                currentSessionService;

        this.refreshCookieService =
                refreshCookieService;

        this.securityProperties =
                securityProperties;

    }


    @PostMapping(
            "/register"
    )
    public ResponseEntity<AuthSessionResponse> register(

            @Valid
            @RequestBody
            RegisterRequest request

    ) {

        return sessionResponse(

                authenticationService
                        .register(
                                request
                        )

        );

    }


    @PostMapping(
            "/login"
    )
    public ResponseEntity<AuthSessionResponse> login(

            @Valid
            @RequestBody
            LoginRequest request

    ) {

        return sessionResponse(

                authenticationService
                        .login(
                                request
                        )

        );

    }


    @PostMapping(
            "/refresh"
    )
    public ResponseEntity<AuthSessionResponse> refresh(
            HttpServletRequest request
    ) {

        String refreshToken =

                readRefreshCookie(
                        request
                );


        if (
                refreshToken == null
        ) {

            throw new ApiAuthenticationException(
                    "Refresh session is unavailable"
            );

        }


        return sessionResponse(

                authenticationService
                        .refresh(
                                refreshToken
                        )

        );

    }


    @PostMapping(
            "/logout"
    )
    public ResponseEntity<Void> logout(
            HttpServletRequest request
    ) {

        authenticationService
                .logout(
                        readRefreshCookie(
                                request
                        )
                );


        return ResponseEntity
                .noContent()
                .header(

                        HttpHeaders.SET_COOKIE,

                        refreshCookieService
                                .clear()
                                .toString()

                )
                .build();

    }


    @GetMapping(
            "/me"
    )
    public AuthUserResponse me() {

        return currentSessionService
                .currentUser();

    }


    private ResponseEntity<AuthSessionResponse>
    sessionResponse(
            IssuedSession issuedSession
    ) {

        return ResponseEntity

                .ok()

                .header(

                        HttpHeaders.SET_COOKIE,

                        refreshCookieService
                                .create(
                                        issuedSession
                                                .refreshToken()
                                )
                                .toString()

                )

                .body(
                        issuedSession.response()
                );

    }


    private String readRefreshCookie(
            HttpServletRequest request
    ) {

        Cookie[] cookies =
                request.getCookies();


        if (
                cookies == null
        ) {

            return null;

        }


        return Arrays
                .stream(
                        cookies
                )
                .filter(
                        cookie ->
                                securityProperties
                                        .refreshCookieName()
                                        .equals(
                                                cookie.getName()
                                        )
                )
                .map(
                        Cookie::getValue
                )
                .findFirst()
                .orElse(
                        null
                );

    }

}
