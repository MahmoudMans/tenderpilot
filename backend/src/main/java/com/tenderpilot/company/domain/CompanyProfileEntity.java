package com.tenderpilot.company.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "company_profiles"
)
public class CompanyProfileEntity {

    @Id
    private UUID id;


    @Column(
            name = "organization_id",
            nullable = false,
            unique = true
    )
    private UUID organizationId;


    @Column(
            name = "legal_name",
            nullable = false,
            length = 220
    )
    private String legalName;


    @Column(
            name = "trading_name",
            length = 220
    )
    private String tradingName;


    @Column(
            name = "industry",
            nullable = false,
            length = 180
    )
    private String industry;


    @Column(
            name = "description",
            nullable = false,
            length = 3000
    )
    private String description;


    @Column(
            name = "website_url",
            length = 500
    )
    private String websiteUrl;


    @Column(
            name = "country_code",
            nullable = false,
            length = 2
    )
    private String countryCode;


    @Column(
            name = "headquarters_city",
            nullable = false,
            length = 160
    )
    private String headquartersCity;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "employee_band",
            nullable = false,
            length = 60
    )
    private EmployeeBand employeeBand;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "annual_revenue_band",
            nullable = false,
            length = 60
    )
    private AnnualRevenueBand annualRevenueBand;


    @Column(
            name = "preferred_min_contract_value",
            precision = 19,
            scale = 2
    )
    private BigDecimal preferredMinContractValue;


    @Column(
            name = "preferred_max_contract_value",
            precision = 19,
            scale = 2
    )
    private BigDecimal preferredMaxContractValue;


    @Column(
            name = "preferred_currency",
            length = 3
    )
    private String preferredCurrency;


    @Column(
            name = "years_experience"
    )
    private Integer yearsExperience;


    @Column(
            name = "minimum_notice_days"
    )
    private Integer minimumNoticeDays;


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "company_capabilities",
            joinColumns = @JoinColumn(
                    name = "profile_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    @Column(
            name = "capability_name",
            nullable = false,
            length = 180
    )
    private List<String> capabilities =
            new ArrayList<>();


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "company_operating_regions",
            joinColumns = @JoinColumn(
                    name = "profile_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<OperatingRegionValue> operatingRegions =
            new ArrayList<>();


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "company_target_cpvs",
            joinColumns = @JoinColumn(
                    name = "profile_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<TargetCpvValue> targetCpvs =
            new ArrayList<>();


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "company_certifications",
            joinColumns = @JoinColumn(
                    name = "profile_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<CertificationValue> certifications =
            new ArrayList<>();


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "company_reference_projects",
            joinColumns = @JoinColumn(
                    name = "profile_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<ReferenceProjectValue> referenceProjects =
            new ArrayList<>();


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "company_preferred_buyer_types",
            joinColumns = @JoinColumn(
                    name = "profile_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "buyer_type",
            nullable = false,
            length = 80
    )
    private List<BuyerType> preferredBuyerTypes =
            new ArrayList<>();


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "company_excluded_keywords",
            joinColumns = @JoinColumn(
                    name = "profile_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    @Column(
            name = "keyword",
            nullable = false,
            length = 180
    )
    private List<String> excludedKeywords =
            new ArrayList<>();


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


    protected CompanyProfileEntity() {
    }


    private CompanyProfileEntity(
            UUID organizationId
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

    }


    public static CompanyProfileEntity create(
            UUID organizationId
    ) {

        return new CompanyProfileEntity(
                organizationId
        );

    }


    public void update(

            String legalName,

            String tradingName,

            String industry,

            String description,

            String websiteUrl,

            String countryCode,

            String headquartersCity,

            EmployeeBand employeeBand,

            AnnualRevenueBand annualRevenueBand,

            BigDecimal preferredMinContractValue,

            BigDecimal preferredMaxContractValue,

            String preferredCurrency,

            Integer yearsExperience,

            Integer minimumNoticeDays,

            List<String> capabilities,

            List<OperatingRegionValue> operatingRegions,

            List<TargetCpvValue> targetCpvs,

            List<CertificationValue> certifications,

            List<ReferenceProjectValue> referenceProjects,

            List<BuyerType> preferredBuyerTypes,

            List<String> excludedKeywords

    ) {

        this.legalName =
                legalName;

        this.tradingName =
                tradingName;

        this.industry =
                industry;

        this.description =
                description;

        this.websiteUrl =
                websiteUrl;

        this.countryCode =
                countryCode;

        this.headquartersCity =
                headquartersCity;

        this.employeeBand =
                employeeBand;

        this.annualRevenueBand =
                annualRevenueBand;

        this.preferredMinContractValue =
                preferredMinContractValue;

        this.preferredMaxContractValue =
                preferredMaxContractValue;

        this.preferredCurrency =
                preferredCurrency;

        this.yearsExperience =
                yearsExperience;

        this.minimumNoticeDays =
                minimumNoticeDays;


        this.capabilities.clear();

        this.capabilities.addAll(
                capabilities
        );


        this.operatingRegions.clear();

        this.operatingRegions.addAll(
                operatingRegions
        );


        this.targetCpvs.clear();

        this.targetCpvs.addAll(
                targetCpvs
        );


        this.certifications.clear();

        this.certifications.addAll(
                certifications
        );


        this.referenceProjects.clear();

        this.referenceProjects.addAll(
                referenceProjects
        );


        this.preferredBuyerTypes.clear();

        this.preferredBuyerTypes.addAll(
                preferredBuyerTypes
        );


        this.excludedKeywords.clear();

        this.excludedKeywords.addAll(
                excludedKeywords
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


        createdAt =
                createdAt == null
                        ? now
                        : createdAt;

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


    public UUID getOrganizationId() {

        return organizationId;

    }


    public String getLegalName() {

        return legalName;

    }


    public String getTradingName() {

        return tradingName;

    }


    public String getIndustry() {

        return industry;

    }


    public String getDescription() {

        return description;

    }


    public String getWebsiteUrl() {

        return websiteUrl;

    }


    public String getCountryCode() {

        return countryCode;

    }


    public String getHeadquartersCity() {

        return headquartersCity;

    }


    public EmployeeBand getEmployeeBand() {

        return employeeBand;

    }


    public AnnualRevenueBand getAnnualRevenueBand() {

        return annualRevenueBand;

    }


    public BigDecimal getPreferredMinContractValue() {

        return preferredMinContractValue;

    }


    public BigDecimal getPreferredMaxContractValue() {

        return preferredMaxContractValue;

    }


    public String getPreferredCurrency() {

        return preferredCurrency;

    }


    public Integer getYearsExperience() {

        return yearsExperience;

    }


    public Integer getMinimumNoticeDays() {

        return minimumNoticeDays;

    }


    public List<String> getCapabilities() {

        return List.copyOf(
                capabilities
        );

    }


    public List<OperatingRegionValue> getOperatingRegions() {

        return List.copyOf(
                operatingRegions
        );

    }


    public List<TargetCpvValue> getTargetCpvs() {

        return List.copyOf(
                targetCpvs
        );

    }


    public List<CertificationValue> getCertifications() {

        return List.copyOf(
                certifications
        );

    }


    public List<ReferenceProjectValue> getReferenceProjects() {

        return List.copyOf(
                referenceProjects
        );

    }


    public List<BuyerType> getPreferredBuyerTypes() {

        return List.copyOf(
                preferredBuyerTypes
        );

    }


    public List<String> getExcludedKeywords() {

        return List.copyOf(
                excludedKeywords
        );

    }


    public Instant getCreatedAt() {

        return createdAt;

    }


    public Instant getUpdatedAt() {

        return updatedAt;

    }

}
