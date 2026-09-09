package com.tenderpilot.organization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "organizations"
)
public class OrganizationEntity {

    @Id
    private UUID id;


    @Column(
            name = "name",
            nullable = false,
            length = 180
    )
    private String name;


    @Column(
            name = "slug",
            nullable = false,
            unique = true,
            length = 190
    )
    private String slug;


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


    protected OrganizationEntity() {
    }


    private OrganizationEntity(

            UUID id,

            String name,

            String slug

    ) {

        this.id =
                id;

        this.name =
                name;

        this.slug =
                slug;

    }


    public static OrganizationEntity create(

            String name,

            String slug

    ) {

        return new OrganizationEntity(

                UUID.randomUUID(),

                name,

                slug

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


    public String getName() {

        return name;

    }


    public String getSlug() {

        return slug;

    }


    public Instant getCreatedAt() {

        return createdAt;

    }


    public Instant getUpdatedAt() {

        return updatedAt;

    }

}
