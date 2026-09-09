package com.tenderpilot.organization.domain;

import com.tenderpilot.identity.domain.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "organization_memberships",
        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_membership_org_user",
                        columnNames = {
                                "organization_id",
                                "user_id"
                        }
                )

        }
)
public class OrganizationMembershipEntity {

    @Id
    private UUID id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private OrganizationEntity organization;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private UserEntity user;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "role",
            nullable = false,
            length = 40
    )
    private MembershipRole role;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected OrganizationMembershipEntity() {
    }


    private OrganizationMembershipEntity(

            UUID id,

            OrganizationEntity organization,

            UserEntity user,

            MembershipRole role

    ) {

        this.id =
                id;

        this.organization =
                organization;

        this.user =
                user;

        this.role =
                role;

    }


    public static OrganizationMembershipEntity create(

            OrganizationEntity organization,

            UserEntity user,

            MembershipRole role

    ) {

        return new OrganizationMembershipEntity(

                UUID.randomUUID(),

                organization,

                user,

                role

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


    public UUID getId() {

        return id;

    }


    public OrganizationEntity getOrganization() {

        return organization;

    }


    public UserEntity getUser() {

        return user;

    }


    public MembershipRole getRole() {

        return role;

    }


    public Instant getCreatedAt() {

        return createdAt;

    }

}
