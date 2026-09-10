package com.tenderpilot.compliance.domain;

import com.tenderpilot.bidworkspace.domain.BidRequirementEntity;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_compliance_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bid_compliance_requirement",
                columnNames = {
                        "requirement_id"
                }
        )
)
public class BidComplianceReviewEntity {

    @Id
    private UUID id;


    @Column(
            name = "organization_id",
            nullable = false
    )
    private UUID organizationId;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "tender_id",
            nullable = false
    )
    private TenderEntity tender;


    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "requirement_id",
            nullable = false,
            unique = true
    )
    private BidRequirementEntity requirement;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "verdict",
            nullable = false,
            length = 40
    )
    private ComplianceVerdict verdict;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "risk_level",
            nullable = false,
            length = 40
    )
    private ComplianceRiskLevel riskLevel;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "evidence_strength",
            nullable = false,
            length = 40
    )
    private EvidenceStrength evidenceStrength;


    @Column(
            name = "review_rationale",
            length = 3000
    )
    private String reviewRationale;


    @Column(
            name = "mitigation_action",
            length = 3000
    )
    private String mitigationAction;


    @Column(
            name = "reviewed_by",
            nullable = false,
            length = 320
    )
    private String reviewedBy;


    @Column(
            name = "reviewed_at",
            nullable = false
    )
    private Instant reviewedAt;


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


    protected BidComplianceReviewEntity() {
    }


    private BidComplianceReviewEntity(
            UUID organizationId,
            TenderEntity tender,
            BidRequirementEntity requirement
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.requirement =
                requirement;

        this.verdict =
                ComplianceVerdict.UNASSESSED;

        this.riskLevel =
                ComplianceRiskLevel.UNASSESSED;

        this.evidenceStrength =
                EvidenceStrength.NONE;

        this.reviewedBy =
                "unknown";

        this.reviewedAt =
                Instant.now();
    }


    public static BidComplianceReviewEntity create(
            UUID organizationId,
            TenderEntity tender,
            BidRequirementEntity requirement
    ) {

        return new BidComplianceReviewEntity(
                organizationId,
                tender,
                requirement
        );
    }


    public void review(
            ComplianceVerdict verdict,
            ComplianceRiskLevel riskLevel,
            EvidenceStrength evidenceStrength,
            String reviewRationale,
            String mitigationAction,
            String reviewedBy
    ) {

        this.verdict =
                verdict;

        this.riskLevel =
                riskLevel;

        this.evidenceStrength =
                evidenceStrength;

        this.reviewRationale =
                reviewRationale;

        this.mitigationAction =
                mitigationAction;

        this.reviewedBy =
                reviewedBy;

        this.reviewedAt =
                Instant.now();
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


        if (
                reviewedAt == null
        ) {

            reviewedAt =
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


    public UUID getOrganizationId() {
        return organizationId;
    }


    public TenderEntity getTender() {
        return tender;
    }


    public BidRequirementEntity getRequirement() {
        return requirement;
    }


    public ComplianceVerdict getVerdict() {
        return verdict;
    }


    public ComplianceRiskLevel getRiskLevel() {
        return riskLevel;
    }


    public EvidenceStrength getEvidenceStrength() {
        return evidenceStrength;
    }


    public String getReviewRationale() {
        return reviewRationale;
    }


    public String getMitigationAction() {
        return mitigationAction;
    }


    public String getReviewedBy() {
        return reviewedBy;
    }


    public Instant getReviewedAt() {
        return reviewedAt;
    }

}
