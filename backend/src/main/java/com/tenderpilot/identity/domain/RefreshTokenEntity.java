package com.tenderpilot.identity.domain;

import com.tenderpilot.organization.domain.OrganizationEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "auth_refresh_tokens"
)
public class RefreshTokenEntity {

    @Id
    private UUID id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private UserEntity user;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private OrganizationEntity organization;


    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;


    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    @Column(
            name = "last_used_at"
    )
    private Instant lastUsedAt;


    @Column(
            name = "revoked_at"
    )
    private Instant revokedAt;


    protected RefreshTokenEntity() {
    }


    private RefreshTokenEntity(

            UUID id,

            UserEntity user,

            OrganizationEntity organization,

            String tokenHash,

            Instant expiresAt

    ) {

        this.id =
                id;

        this.user =
                user;

        this.organization =
                organization;

        this.tokenHash =
                tokenHash;

        this.expiresAt =
                expiresAt;

    }


    public static RefreshTokenEntity create(

            UserEntity user,

            OrganizationEntity organization,

            String tokenHash,

            Instant expiresAt

    ) {

        return new RefreshTokenEntity(

                UUID.randomUUID(),

                user,

                organization,

                tokenHash,

                expiresAt

        );

    }


    @PrePersist
    void beforeInsert() {

        if (
                id == null
        ) {

            id =
                    UUID.randomUUID();

        }


        if (
                createdAt == null
        ) {

            createdAt =
                    Instant.now();

        }

    }


    public void consume() {

        Instant now =
                Instant.now();

        lastUsedAt =
                now;

        revokedAt =
                now;

    }


    public void revoke() {

        if (
                revokedAt == null
        ) {

            revokedAt =
                    Instant.now();

        }

    }


    public boolean isUsableAt(
            Instant instant
    ) {

        return revokedAt == null
                &&
                expiresAt.isAfter(
                        instant
                );

    }


    public UUID getId() {

        return id;

    }


    public UserEntity getUser() {

        return user;

    }


    public OrganizationEntity getOrganization() {

        return organization;

    }


    public String getTokenHash() {

        return tokenHash;

    }


    public Instant getExpiresAt() {

        return expiresAt;

    }


    public Instant getCreatedAt() {

        return createdAt;

    }


    public Instant getLastUsedAt() {

        return lastUsedAt;

    }


    public Instant getRevokedAt() {

        return revokedAt;

    }

}
