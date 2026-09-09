package com.tenderpilot.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.common.domain.ApiAuthenticationException;
import com.tenderpilot.common.domain.ForbiddenOperationException;

import com.tenderpilot.identity.api.LoginRequest;
import com.tenderpilot.identity.api.RegisterRequest;

import com.tenderpilot.identity.application.AuthenticationService;
import com.tenderpilot.identity.application.IssuedSession;

import com.tenderpilot.identity.infrastructure.UserRepository;

import com.tenderpilot.organization.application.OrganizationService;
import com.tenderpilot.organization.domain.MembershipRole;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles(
        "test"
)
@Transactional
class IdentityFlowIntegrationTest {

    @Autowired
    private AuthenticationService
            authenticationService;


    @Autowired
    private UserRepository
            userRepository;


    @Autowired
    private JwtDecoder
            jwtDecoder;


    @Autowired
    private OrganizationService
            organizationService;


    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder
                .clearContext();

    }


    @Test
    void registrationCreatesOwnerAndHashesPassword() {

        IssuedSession session =

                authenticationService
                        .register(
                                new RegisterRequest(

                                        "Alice Martin",

                                        "Aurelia Renovation",

                                        "alice@example.test",

                                        "VeryStrongPassword123!"

                                )
                        );


        assertThat(
                session
                        .response()
                        .user()
                        .role()
        )
                .isEqualTo(
                        MembershipRole.OWNER
                );


        assertThat(
                session
                        .response()
                        .accessToken()
        )
                .isNotBlank();


        assertThat(
                session.refreshToken()
        )
                .isNotBlank();


        var persistedUser =

                userRepository
                        .findByNormalizedEmail(
                                "alice@example.test"
                        )
                        .orElseThrow();


        assertThat(
                persistedUser.getPasswordHash()
        )
                .startsWith(
                        "{bcrypt}"
                )
                .doesNotContain(
                        "VeryStrongPassword123!"
                );

    }


    @Test
    void loginRejectsIncorrectPassword() {

        authenticationService
                .register(
                        new RegisterRequest(

                                "Alice Martin",

                                "Aurelia Renovation",

                                "alice@example.test",

                                "VeryStrongPassword123!"

                        )
                );


        assertThatThrownBy(
                () ->
                        authenticationService
                                .login(
                                        new LoginRequest(

                                                "alice@example.test",

                                                "WrongPassword123!"

                                        )
                                )
        )
                .isInstanceOf(
                        ApiAuthenticationException.class
                );

    }


    @Test
    void refreshTokenRotatesAndOldTokenCannotBeReused() {

        IssuedSession firstSession =

                authenticationService
                        .register(
                                new RegisterRequest(

                                        "Alice Martin",

                                        "Aurelia Renovation",

                                        "alice@example.test",

                                        "VeryStrongPassword123!"

                                )
                        );


        IssuedSession rotatedSession =

                authenticationService
                        .refresh(
                                firstSession.refreshToken()
                        );


        assertThat(
                rotatedSession.refreshToken()
        )
                .isNotEqualTo(
                        firstSession.refreshToken()
                );


        assertThatThrownBy(
                () ->
                        authenticationService
                                .refresh(
                                        firstSession.refreshToken()
                                )
        )
                .isInstanceOf(
                        ApiAuthenticationException.class
                );

    }


    @Test
    void tenantCannotReadAnotherOrganization() {

        IssuedSession organizationA =

                authenticationService
                        .register(
                                new RegisterRequest(

                                        "Alice Martin",

                                        "Aurelia Renovation",

                                        "alice@example.test",

                                        "VeryStrongPassword123!"

                                )
                        );


        IssuedSession organizationB =

                authenticationService
                        .register(
                                new RegisterRequest(

                                        "Bob Bernard",

                                        "Boreal Construction",

                                        "bob@example.test",

                                        "AnotherStrongPassword123!"

                                )
                        );


        authenticate(
                organizationA
                        .response()
                        .accessToken()
        );


        UUID ownOrganizationId =

                organizationA
                        .response()
                        .user()
                        .organization()
                        .id();


        UUID foreignOrganizationId =

                organizationB
                        .response()
                        .user()
                        .organization()
                        .id();


        assertThat(
                organizationService
                        .getForCurrentTenant(
                                ownOrganizationId
                        )
                        .id()
        )
                .isEqualTo(
                        ownOrganizationId
                );


        assertThatThrownBy(
                () ->
                        organizationService
                                .getForCurrentTenant(
                                        foreignOrganizationId
                                )
        )
                .isInstanceOf(
                        ForbiddenOperationException.class
                );

    }


    private void authenticate(
            String accessToken
    ) {

        Jwt jwt =
                jwtDecoder
                        .decode(
                                accessToken
                        );


        SecurityContext context =

                SecurityContextHolder
                        .createEmptyContext();


        JwtAuthenticationToken authentication =

                new JwtAuthenticationToken(

                        jwt,

                        List.of()

                );


        assertThat(
                authentication.isAuthenticated()
        )
                .as(
                        "Test JWT authentication fixture must represent an authenticated resource-server principal"
                )
                .isTrue();


        context.setAuthentication(
                authentication
        );


        SecurityContextHolder
                .setContext(
                        context
                );

    }

}
