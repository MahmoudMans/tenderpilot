package com.tenderpilot.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "app_users"
)
public class UserEntity {

    @Id
    private UUID id;


    @Column(
            name = "email",
            nullable = false,
            length = 320
    )
    private String email;


    @Column(
            name = "normalized_email",
            nullable = false,
            unique = true,
            length = 320
    )
    private String normalizedEmail;


    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;


    @Column(
            name = "full_name",
            nullable = false,
            length = 160
    )
    private String fullName;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private UserStatus status;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;


    protected UserEntity() {
    }


    private UserEntity(

            UUID id,

            String email,

            String normalizedEmail,

            String passwordHash,

            String fullName,

            UserStatus status

    ) {

        this.id =
                id;

        this.email =
                email;

        this.normalizedEmail =
                normalizedEmail;

        this.passwordHash =
                passwordHash;

        this.fullName =
                fullName;

        this.status =
                status;

    }


    public static UserEntity create(

            String email,

            String normalizedEmail,

            String passwordHash,

            String fullName

    ) {

        return new UserEntity(

                UUID.randomUUID(),

                email,

                normalizedEmail,

                passwordHash,

                fullName,

                UserStatus.ACTIVE

        );

    }


    @PrePersist
    void beforeInsert() {

        Instant now =
                Instant.now();


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
                    now;

        }


        updatedAt =
                now;

    }


    @PreUpdate
    void beforeUpdate() {

        updatedAt =
                Instant.now();

    }


    public UUID getId() {

        return id;

    }


    public String getEmail() {

        return email;

    }


    public String getNormalizedEmail() {

        return normalizedEmail;

    }


    public String getPasswordHash() {

        return passwordHash;

    }


    public String getFullName() {

        return fullName;

    }


    public UserStatus getStatus() {

        return status;

    }


    public Instant getCreatedAt() {

        return createdAt;

    }


    public Instant getUpdatedAt() {

        return updatedAt;

    }

}
