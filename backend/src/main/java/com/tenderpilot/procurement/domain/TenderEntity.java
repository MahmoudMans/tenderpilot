package com.tenderpilot.procurement.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "procurement_tenders"
)
public class TenderEntity {

    @Id
    private UUID id;


    @Column(
            name = "canonical_key",
            nullable = false,
            unique = true,
            length = 320
    )
    private String canonicalKey;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "buyer_id",
            nullable = false
    )
    private ProcurementBuyerEntity buyer;


    @Column(
            name = "title",
            nullable = false,
            length = 1000
    )
    private String title;


    @Column(
            name = "description",
            nullable = false,
            length = 12000
    )
    private String description;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "status",
            nullable = false,
            length = 60
    )
    private ProcurementStatus status;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "procedure_type",
            nullable = false,
            length = 80
    )
    private ProcurementProcedure procedureType;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "contract_nature",
            nullable = false,
            length = 40
    )
    private ContractNature contractNature;


    @Column(
            name = "publication_date",
            nullable = false
    )
    private LocalDate publicationDate;


    @Column(
            name = "submission_deadline"
    )
    private Instant submissionDeadline;


    @Column(
            name = "estimated_min_value",
            precision = 19,
            scale = 2
    )
    private BigDecimal estimatedMinValue;


    @Column(
            name = "estimated_max_value",
            precision = 19,
            scale = 2
    )
    private BigDecimal estimatedMaxValue;


    @Column(
            name = "currency",
            length = 3
    )
    private String currency;


    @Column(
            name = "country_code",
            nullable = false,
            length = 2
    )
    private String countryCode;


    @Column(
            name = "primary_region_code",
            length = 40
    )
    private String primaryRegionCode;


    @Column(
            name = "primary_region_name",
            length = 180
    )
    private String primaryRegionName;


    @Column(
            name = "framework_agreement",
            nullable = false
    )
    private boolean frameworkAgreement;


    @Column(
            name = "joint_procurement",
            nullable = false
    )
    private boolean jointProcurement;


    @Column(
            name = "electronic_submission",
            nullable = false
    )
    private boolean electronicSubmission;


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "procurement_tender_cpvs",
            joinColumns = @JoinColumn(
                    name = "tender_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<CpvClassificationValue> cpvs =
            new ArrayList<>();


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "procurement_tender_locations",
            joinColumns = @JoinColumn(
                    name = "tender_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<ProcurementLocationValue> locations =
            new ArrayList<>();


    @OneToMany(
            mappedBy = "tender",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TenderLotEntity> lots =
            new ArrayList<>();


    @OneToMany(
            mappedBy = "tender",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TenderSourceRecordEntity> sourceRecords =
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


    protected TenderEntity() {
    }


    private TenderEntity(
            String canonicalKey
    ) {

        this.id =
                UUID.randomUUID();

        this.canonicalKey =
                canonicalKey;

    }


    public static TenderEntity create(
            String canonicalKey
    ) {

        return new TenderEntity(
                canonicalKey
        );

    }


    public void update(

            ProcurementBuyerEntity buyer,

            String title,

            String description,

            ProcurementStatus status,

            ProcurementProcedure procedureType,

            ContractNature contractNature,

            LocalDate publicationDate,

            Instant submissionDeadline,

            BigDecimal estimatedMinValue,

            BigDecimal estimatedMaxValue,

            String currency,

            String countryCode,

            String primaryRegionCode,

            String primaryRegionName,

            boolean frameworkAgreement,

            boolean jointProcurement,

            boolean electronicSubmission

    ) {

        this.buyer =
                buyer;

        this.title =
                title;

        this.description =
                description;

        this.status =
                status;

        this.procedureType =
                procedureType;

        this.contractNature =
                contractNature;

        this.publicationDate =
                publicationDate;

        this.submissionDeadline =
                submissionDeadline;

        this.estimatedMinValue =
                estimatedMinValue;

        this.estimatedMaxValue =
                estimatedMaxValue;

        this.currency =
                currency;

        this.countryCode =
                countryCode;

        this.primaryRegionCode =
                primaryRegionCode;

        this.primaryRegionName =
                primaryRegionName;

        this.frameworkAgreement =
                frameworkAgreement;

        this.jointProcurement =
                jointProcurement;

        this.electronicSubmission =
                electronicSubmission;

    }


    public void replaceCpvs(
            List<CpvClassificationValue> values
    ) {

        cpvs.clear();

        cpvs.addAll(
                values
        );

    }


    public void replaceLocations(
            List<ProcurementLocationValue> values
    ) {

        locations.clear();

        locations.addAll(
                values
        );

    }


    public void replaceLots(
            List<TenderLotEntity> values
    ) {

        lots.clear();

        lots.addAll(
                values
        );

    }


    public void addSourceRecord(
            TenderSourceRecordEntity sourceRecord
    ) {

        sourceRecords.add(
                sourceRecord
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


    public String getCanonicalKey() {

        return canonicalKey;

    }


    public ProcurementBuyerEntity getBuyer() {

        return buyer;

    }


    public String getTitle() {

        return title;

    }


    public String getDescription() {

        return description;

    }


    public ProcurementStatus getStatus() {

        return status;

    }


    public ProcurementProcedure getProcedureType() {

        return procedureType;

    }


    public ContractNature getContractNature() {

        return contractNature;

    }


    public LocalDate getPublicationDate() {

        return publicationDate;

    }


    public Instant getSubmissionDeadline() {

        return submissionDeadline;

    }


    public BigDecimal getEstimatedMinValue() {

        return estimatedMinValue;

    }


    public BigDecimal getEstimatedMaxValue() {

        return estimatedMaxValue;

    }


    public String getCurrency() {

        return currency;

    }


    public String getCountryCode() {

        return countryCode;

    }


    public String getPrimaryRegionCode() {

        return primaryRegionCode;

    }


    public String getPrimaryRegionName() {

        return primaryRegionName;

    }


    public boolean isFrameworkAgreement() {

        return frameworkAgreement;

    }


    public boolean isJointProcurement() {

        return jointProcurement;

    }


    public boolean isElectronicSubmission() {

        return electronicSubmission;

    }


    public List<CpvClassificationValue> getCpvs() {

        return List.copyOf(
                cpvs
        );

    }


    public List<ProcurementLocationValue> getLocations() {

        return List.copyOf(
                locations
        );

    }


    public List<TenderLotEntity> getLots() {

        return List.copyOf(
                lots
        );

    }


    public List<TenderSourceRecordEntity> getSourceRecords() {

        return List.copyOf(
                sourceRecords
        );

    }


    public Instant getCreatedAt() {

        return createdAt;

    }


    public Instant getUpdatedAt() {

        return updatedAt;

    }

}
