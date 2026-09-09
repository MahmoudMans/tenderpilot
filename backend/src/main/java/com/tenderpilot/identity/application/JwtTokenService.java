package com.tenderpilot.identity.application;

import com.tenderpilot.identity.domain.UserEntity;
import com.tenderpilot.organization.domain.MembershipRole;
import com.tenderpilot.organization.domain.OrganizationEntity;
import com.tenderpilot.security.SecurityProperties;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    private final SecurityProperties
            securityProperties;


    public JwtTokenService(

            JwtEncoder jwtEncoder,

            SecurityProperties securityProperties

    ) {

        this.jwtEncoder =
                jwtEncoder;

        this.securityProperties =
                securityProperties;

    }


    public IssuedAccessToken issue(

            UserEntity user,

            OrganizationEntity organization,

            MembershipRole role

    ) {

        Instant issuedAt =
                Instant.now();


        Instant expiresAt =
                issuedAt.plus(
                        securityProperties
                                .accessTokenTtl()
                );


        JwtClaimsSet claims =

                JwtClaimsSet
                        .builder()

                        .issuer(
                                securityProperties
                                        .issuer()
                        )

                        .subject(
                                user
                                        .getId()
                                        .toString()
                        )

                        .issuedAt(
                                issuedAt
                        )

                        .expiresAt(
                                expiresAt
                        )

                        .id(
                                UUID
                                        .randomUUID()
                                        .toString()
                        )

                        .claim(
                                "token_type",
                                "access"
                        )

                        .claim(
                                "email",
                                user.getEmail()
                        )

                        .claim(
                                "name",
                                user.getFullName()
                        )

                        .claim(
                                "org_id",
                                organization
                                        .getId()
                                        .toString()
                        )

                        .claim(
                                "org_name",
                                organization.getName()
                        )

                        .claim(
                                "role",
                                role.name()
                        )

                        .build();


        JwsHeader header =

                JwsHeader
                        .with(
                                MacAlgorithm.HS256
                        )
                        .type(
                                "JWT"
                        )
                        .build();


        String value =

                jwtEncoder
                        .encode(
                                JwtEncoderParameters
                                        .from(
                                                header,
                                                claims
                                        )
                        )
                        .getTokenValue();


        return new IssuedAccessToken(

                value,

                expiresAt,

                securityProperties
                        .accessTokenTtl()
                        .toSeconds()

        );

    }

}
