package com.tenderpilot.identity.application;

import com.tenderpilot.identity.domain.RefreshTokenEntity;
import com.tenderpilot.identity.domain.UserEntity;
import com.tenderpilot.identity.infrastructure.RefreshTokenRepository;
import com.tenderpilot.organization.domain.OrganizationEntity;
import com.tenderpilot.security.SecurityProperties;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.time.Instant;

import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES =
            32;


    private final SecureRandom secureRandom =
            new SecureRandom();


    private final RefreshTokenRepository
            refreshTokenRepository;


    private final SecurityProperties
            securityProperties;


    public RefreshTokenService(

            RefreshTokenRepository refreshTokenRepository,

            SecurityProperties securityProperties

    ) {

        this.refreshTokenRepository =
                refreshTokenRepository;

        this.securityProperties =
                securityProperties;

    }


    public CreatedRefreshToken create(

            UserEntity user,

            OrganizationEntity organization

    ) {

        byte[] randomBytes =
                new byte[
                        TOKEN_BYTES
                        ];


        secureRandom.nextBytes(
                randomBytes
        );


        String rawToken =

                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                randomBytes
                        );


        RefreshTokenEntity entity =

                RefreshTokenEntity
                        .create(

                                user,

                                organization,

                                hash(
                                        rawToken
                                ),

                                Instant
                                        .now()
                                        .plus(
                                                securityProperties
                                                        .refreshTokenTtl()
                                        )

                        );


        refreshTokenRepository
                .save(
                        entity
                );


        return new CreatedRefreshToken(

                rawToken,

                entity

        );

    }


    public Optional<RefreshTokenEntity> find(
            String rawToken
    ) {

        if (
                rawToken == null
                ||
                rawToken.isBlank()
        ) {

            return Optional.empty();

        }


        return refreshTokenRepository
                .findByTokenHash(
                        hash(
                                rawToken
                        )
                );

    }


    public void revokeIfPresent(
            String rawToken
    ) {

        find(
                rawToken
        )
                .ifPresent(
                        token -> {

                            token.revoke();

                            refreshTokenRepository
                                    .save(
                                            token
                                    );

                        }
                );

    }


    private String hash(
            String rawToken
    ) {

        try {

            MessageDigest digest =
                    MessageDigest
                            .getInstance(
                                    "SHA-256"
                            );


            byte[] hashed =

                    digest.digest(

                            rawToken
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )

                    );


            return HexFormat
                    .of()
                    .formatHex(
                            hashed
                    );


        } catch (
                NoSuchAlgorithmException exception
        ) {

            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );

        }

    }

}
