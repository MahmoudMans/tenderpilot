package com.tenderpilot.matching.security;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.stereotype.Component;

@Component
public class CurrentOrganizationResolver {

    private static final List<String> ORGANIZATION_CLAIMS =
            List.of(
                    "organizationId",
                    "organization_id",
                    "orgId",
                    "org_id",
                    "organization",
                    "org"
            );


    private final JdbcTemplate jdbcTemplate;


    public CurrentOrganizationResolver(
            JdbcTemplate jdbcTemplate
    ) {

        this.jdbcTemplate =
                jdbcTemplate;
    }


    public UUID resolve(
            Authentication authentication
    ) {

        if (
                authentication == null
                ||
                !authentication.isAuthenticated()
        ) {

            throw new AccessDeniedException(
                    "Authenticated organization context is required"
            );
        }


        Jwt jwt =
                extractJwt(
                        authentication
                );


        if (
                jwt != null
        ) {

            UUID fromClaim =
                    organizationClaim(
                            jwt.getClaims()
                    );


            if (
                    fromClaim != null
            ) {

                return fromClaim;
            }


            UUID subjectUserId =
                    uuid(
                            jwt.getSubject()
                    );


            if (
                    subjectUserId != null
            ) {

                UUID membership =
                        singleMembershipForUser(
                                subjectUserId
                        );


                if (
                        membership != null
                ) {

                    return membership;
                }
            }


            String email =
                    jwt.getClaimAsString(
                            "email"
                    );


            if (
                    email != null
            ) {

                UUID membership =
                        singleMembershipForEmail(
                                email
                        );


                if (
                        membership != null
                ) {

                    return membership;
                }
            }
        }


        throw new AccessDeniedException(
                "Unable to resolve the active organization"
        );
    }


    private Jwt extractJwt(
            Authentication authentication
    ) {

        if (
                authentication
                        instanceof
                        JwtAuthenticationToken token
        ) {

            return token.getToken();
        }


        if (
                authentication.getPrincipal()
                        instanceof
                        Jwt jwt
        ) {

            return jwt;
        }


        return null;
    }


    private UUID organizationClaim(
            Map<String, Object> claims
    ) {

        for (
                String name :
                ORGANIZATION_CLAIMS
        ) {

            UUID candidate =
                    uuid(
                            claims.get(
                                    name
                            )
                    );


            if (
                    candidate != null
            ) {

                return candidate;
            }
        }


        return null;
    }


    private UUID singleMembershipForUser(
            UUID userId
    ) {

        List<UUID> memberships =
                jdbcTemplate
                        .queryForList(
                                """
                                select organization_id
                                from organization_memberships
                                where user_id = ?
                                """,
                                UUID.class,
                                userId
                        );


        return singleMembership(
                memberships
        );
    }


    private UUID singleMembershipForEmail(
            String email
    ) {

        List<UUID> memberships =
                jdbcTemplate
                        .queryForList(
                                """
                                select m.organization_id
                                from organization_memberships m
                                join app_users u
                                  on u.id = m.user_id
                                where u.normalized_email = ?
                                """,
                                UUID.class,
                                email
                                        .trim()
                                        .toLowerCase()
                        );


        return singleMembership(
                memberships
        );
    }


    private UUID singleMembership(
            List<UUID> memberships
    ) {

        if (
                memberships.isEmpty()
        ) {

            return null;
        }


        if (
                memberships.size()
                        >
                        1
        ) {

            throw new AccessDeniedException(
                    "Multiple organization memberships require an active organization JWT claim"
            );
        }


        return memberships.getFirst();
    }


    private UUID uuid(
            Object value
    ) {

        if (
                value instanceof UUID uuid
        ) {

            return uuid;
        }


        if (
                value == null
        ) {

            return null;
        }


        try {

            return UUID.fromString(
                    value.toString()
            );

        } catch (
                IllegalArgumentException exception
        ) {

            return null;
        }
    }

}
