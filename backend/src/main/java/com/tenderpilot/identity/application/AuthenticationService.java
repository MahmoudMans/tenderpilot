package com.tenderpilot.identity.application;

import com.tenderpilot.common.domain.ApiAuthenticationException;
import com.tenderpilot.common.domain.ConflictException;

import com.tenderpilot.identity.api.AuthSessionResponse;
import com.tenderpilot.identity.api.LoginRequest;
import com.tenderpilot.identity.api.RegisterRequest;

import com.tenderpilot.identity.domain.RefreshTokenEntity;
import com.tenderpilot.identity.domain.UserEntity;
import com.tenderpilot.identity.domain.UserStatus;

import com.tenderpilot.identity.infrastructure.RefreshTokenRepository;
import com.tenderpilot.identity.infrastructure.UserRepository;

import com.tenderpilot.organization.application.OrganizationSlugService;

import com.tenderpilot.organization.domain.MembershipRole;
import com.tenderpilot.organization.domain.OrganizationEntity;
import com.tenderpilot.organization.domain.OrganizationMembershipEntity;

import com.tenderpilot.organization.infrastructure.OrganizationMembershipRepository;
import com.tenderpilot.organization.infrastructure.OrganizationRepository;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final String GENERIC_LOGIN_ERROR =
            "Invalid email or password";


    private final UserRepository
            userRepository;


    private final OrganizationRepository
            organizationRepository;


    private final OrganizationMembershipRepository
            membershipRepository;


    private final RefreshTokenRepository
            refreshTokenRepository;


    private final PasswordEncoder
            passwordEncoder;


    private final IdentityNormalizer
            identityNormalizer;


    private final OrganizationSlugService
            organizationSlugService;


    private final JwtTokenService
            jwtTokenService;


    private final RefreshTokenService
            refreshTokenService;


    private final AuthResponseFactory
            authResponseFactory;


    public AuthenticationService(

            UserRepository userRepository,

            OrganizationRepository organizationRepository,

            OrganizationMembershipRepository membershipRepository,

            RefreshTokenRepository refreshTokenRepository,

            PasswordEncoder passwordEncoder,

            IdentityNormalizer identityNormalizer,

            OrganizationSlugService organizationSlugService,

            JwtTokenService jwtTokenService,

            RefreshTokenService refreshTokenService,

            AuthResponseFactory authResponseFactory

    ) {

        this.userRepository =
                userRepository;

        this.organizationRepository =
                organizationRepository;

        this.membershipRepository =
                membershipRepository;

        this.refreshTokenRepository =
                refreshTokenRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.identityNormalizer =
                identityNormalizer;

        this.organizationSlugService =
                organizationSlugService;

        this.jwtTokenService =
                jwtTokenService;

        this.refreshTokenService =
                refreshTokenService;

        this.authResponseFactory =
                authResponseFactory;

    }


    @Transactional
    public IssuedSession register(
            RegisterRequest request
    ) {

        String normalizedEmail =

                identityNormalizer
                        .normalizeEmail(
                                request.email()
                        );


        if (
                userRepository
                        .existsByNormalizedEmail(
                                normalizedEmail
                        )
        ) {

            throw new ConflictException(
                    "An account already exists for this email address"
            );

        }


        String fullName =

                identityNormalizer
                        .cleanDisplayText(
                                request.fullName()
                        );


        String organizationName =

                identityNormalizer
                        .cleanDisplayText(
                                request.organizationName()
                        );


        UserEntity user =

                UserEntity
                        .create(

                                normalizedEmail,

                                normalizedEmail,

                                passwordEncoder
                                        .encode(
                                                request.password()
                                        ),

                                fullName

                        );


        userRepository
                .save(
                        user
                );


        OrganizationEntity organization =

                OrganizationEntity
                        .create(

                                organizationName,

                                organizationSlugService
                                        .generate(
                                                organizationName
                                        )

                        );


        organizationRepository
                .save(
                        organization
                );


        OrganizationMembershipEntity membership =

                OrganizationMembershipEntity
                        .create(

                                organization,

                                user,

                                MembershipRole.OWNER

                        );


        membershipRepository
                .save(
                        membership
                );


        return issueSession(

                user,

                organization,

                MembershipRole.OWNER

        );

    }


    @Transactional
    public IssuedSession login(
            LoginRequest request
    ) {

        String normalizedEmail =

                identityNormalizer
                        .normalizeEmail(
                                request.email()
                        );


        UserEntity user =

                userRepository
                        .findByNormalizedEmail(
                                normalizedEmail
                        )
                        .orElseThrow(
                                () ->
                                        new ApiAuthenticationException(
                                                GENERIC_LOGIN_ERROR
                                        )
                        );


        if (
                user.getStatus()
                        !=
                        UserStatus.ACTIVE
        ) {

            throw new ApiAuthenticationException(
                    GENERIC_LOGIN_ERROR
            );

        }


        if (
                !passwordEncoder
                        .matches(

                                request.password(),

                                user.getPasswordHash()

                        )
        ) {

            throw new ApiAuthenticationException(
                    GENERIC_LOGIN_ERROR
            );

        }


        OrganizationMembershipEntity membership =

                membershipRepository
                        .findFirstByUser_IdOrderByCreatedAtAsc(
                                user.getId()
                        )
                        .orElseThrow(
                                () ->
                                        new ApiAuthenticationException(
                                                "Account is not attached to an organization"
                                        )
                        );


        return issueSession(

                user,

                membership.getOrganization(),

                membership.getRole()

        );

    }


    @Transactional
    public IssuedSession refresh(
            String rawRefreshToken
    ) {

        RefreshTokenEntity existing =

                refreshTokenService
                        .find(
                                rawRefreshToken
                        )
                        .orElseThrow(
                                () ->
                                        new ApiAuthenticationException(
                                                "Refresh session is invalid"
                                        )
                        );


        if (
                !existing
                        .isUsableAt(
                                Instant.now()
                        )
        ) {

            throw new ApiAuthenticationException(
                    "Refresh session has expired"
            );

        }


        UserEntity user =
                existing.getUser();


        OrganizationEntity organization =
                existing.getOrganization();


        if (
                user.getStatus()
                        !=
                        UserStatus.ACTIVE
        ) {

            existing.revoke();

            refreshTokenRepository
                    .save(
                            existing
                    );


            throw new ApiAuthenticationException(
                    "Account is not active"
            );

        }


        OrganizationMembershipEntity membership =

                membershipRepository
                        .findByUser_IdAndOrganization_Id(

                                user.getId(),

                                organization.getId()

                        )
                        .orElseThrow(
                                () ->
                                        new ApiAuthenticationException(
                                                "Organization membership is no longer active"
                                        )
                        );


        existing.consume();

        refreshTokenRepository
                .save(
                        existing
                );


        return issueSession(

                user,

                organization,

                membership.getRole()

        );

    }


    @Transactional
    public void logout(
            String rawRefreshToken
    ) {

        refreshTokenService
                .revokeIfPresent(
                        rawRefreshToken
                );

    }


    private IssuedSession issueSession(

            UserEntity user,

            OrganizationEntity organization,

            MembershipRole role

    ) {

        IssuedAccessToken accessToken =

                jwtTokenService
                        .issue(

                                user,

                                organization,

                                role

                        );


        CreatedRefreshToken refreshToken =

                refreshTokenService
                        .create(

                                user,

                                organization

                        );


        AuthSessionResponse response =

                authResponseFactory
                        .session(

                                accessToken,

                                user,

                                organization,

                                role

                        );


        return new IssuedSession(

                response,

                refreshToken.rawToken()

        );

    }

}
