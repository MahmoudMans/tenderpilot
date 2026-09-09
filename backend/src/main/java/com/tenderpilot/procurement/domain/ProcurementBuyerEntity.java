package com.tenderpilot.procurement.domain;

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
        name = "procurement_buyers"
)
public class ProcurementBuyerEntity {

    @Id
    private UUID id;


    @Column(
            name = "canonical_key",
            nullable = false,
            unique = true,
            length = 220
    )
    private String canonicalKey;


    @Column(
            name = "legal_name",
            nullable = false,
            length = 300
    )
    private String legalName;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "buyer_category",
            nullable = false,
            length = 80
    )
    private BuyerCategory category;


    @Column(
            name = "country_code",
            nullable = false,
            length = 2
    )
    private String countryCode;


    @Column(
            name = "city",
            length = 180
    )
    private String city;


    @Column(
            name = "registration_number",
            length = 140
    )
    private String registrationNumber;


    @Column(
            name = "website_url",
            length = 500
    )
    private String websiteUrl;


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


    protected ProcurementBuyerEntity() {
    }


    private ProcurementBuyerEntity(
            String canonicalKey
    ) {

        this.id =
                UUID.randomUUID();

        this.canonicalKey =
                canonicalKey;

    }


    public static ProcurementBuyerEntity create(
            String canonicalKey
    ) {

        return new ProcurementBuyerEntity(
                canonicalKey
        );

    }


    public void update(

            String legalName,

            BuyerCategory category,

            String countryCode,

            String city,

            String registrationNumber,

            String websiteUrl

    ) {

        this.legalName =
                legalName;

        this.category =
                category;

        this.countryCode =
                countryCode;

        this.city =
                city;

        this.registrationNumber =
                registrationNumber;

        this.websiteUrl =
                websiteUrl;

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


    public String getCanonicalKey() {

        return canonicalKey;

    }


    public String getLegalName() {

        return legalName;

    }


    public BuyerCategory getCategory() {

        return category;

    }


    public String getCountryCode() {

        return countryCode;

    }


    public String getCity() {

        return city;

    }


    public String getRegistrationNumber() {

        return registrationNumber;

    }


    public String getWebsiteUrl() {

        return websiteUrl;

    }


    public Instant getCreatedAt() {

        return createdAt;

    }


    public Instant getUpdatedAt() {

        return updatedAt;

    }

}
