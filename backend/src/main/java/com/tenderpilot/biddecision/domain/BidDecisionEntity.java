package com.tenderpilot.biddecision.domain;

import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_decisions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_org_bid_decision_tender",
                columnNames = {
                        "organization_id",
                        "tender_id"
                }
        )
)
public class BidDecisionEntity {

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


    @Enumerated(EnumType.STRING)
    @Column(
            name = "outcome",
            nullable = false,
            length = 30
    )
    private BidDecisionOutcome outcome;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "recommended_outcome",
            nullable = false,
            length = 30
    )
    private BidDecisionOutcome recommendedOutcome;


    @Column(
            name = "decision_score",
            nullable = false
    )
    private int decisionScore;


    @Column(
            name = "match_score_snapshot",
            nullable = false
    )
    private int matchScoreSnapshot;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "match_grade_snapshot",
            nullable = false,
            length = 30
    )
    private TenderMatchGrade matchGradeSnapshot;


    @Column(
            name = "strategic_fit",
            nullable = false
    )
    private int strategicFit;


    @Column(
            name = "capability_confidence",
            nullable = false
    )
    private int capabilityConfidence;


    @Column(
            name = "commercial_attractiveness",
            nullable = false
    )
    private int commercialAttractiveness;


    @Column(
            name = "delivery_feasibility",
            nullable = false
    )
    private int deliveryFeasibility;


    @Column(
            name = "resource_capacity",
            nullable = false
    )
    private int resourceCapacity;


    @Column(
            name = "risk_confidence",
            nullable = false
    )
    private int riskConfidence;


    @Column(
            name = "recommendation_reason",
            nullable = false,
            length = 1200
    )
    private String recommendationReason;


    @Column(
            name = "rationale",
            length = 3000
    )
    private String rationale;


    @Column(
            name = "key_risks",
            length = 3000
    )
    private String keyRisks;


    @Column(
            name = "assumptions",
            length = 3000
    )
    private String assumptions;


    @Column(
            name = "decided_by",
            length = 320
    )
    private String decidedBy;


    @Column(
            name = "decided_at"
    )
    private Instant decidedAt;


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


    protected BidDecisionEntity() {
    }


    private BidDecisionEntity(
            UUID organizationId,
            TenderEntity tender
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.outcome =
                BidDecisionOutcome.PENDING;

        this.recommendedOutcome =
                BidDecisionOutcome.HOLD;

        this.recommendationReason =
                "Assessment has not yet been completed.";
    }


    public static BidDecisionEntity create(
            UUID organizationId,
            TenderEntity tender
    ) {

        return new BidDecisionEntity(
                organizationId,
                tender
        );
    }


    public void applyAssessment(
            BidDecisionScorecard scorecard,
            BidAssessmentEvaluation evaluation,
            int matchScore,
            TenderMatchGrade matchGrade,
            String rationale,
            String keyRisks,
            String assumptions
    ) {

        this.strategicFit =
                scorecard.strategicFit();

        this.capabilityConfidence =
                scorecard.capabilityConfidence();

        this.commercialAttractiveness =
                scorecard.commercialAttractiveness();

        this.deliveryFeasibility =
                scorecard.deliveryFeasibility();

        this.resourceCapacity =
                scorecard.resourceCapacity();

        this.riskConfidence =
                scorecard.riskConfidence();

        this.decisionScore =
                evaluation.score();

        this.recommendedOutcome =
                evaluation.recommendation();

        this.recommendationReason =
                evaluation.recommendationReason();

        this.matchScoreSnapshot =
                matchScore;

        this.matchGradeSnapshot =
                matchGrade;

        this.rationale =
                rationale;

        this.keyRisks =
                keyRisks;

        this.assumptions =
                assumptions;
    }


    public void decide(
            BidDecisionOutcome outcome,
            String actor
    ) {

        this.outcome =
                outcome;

        this.decidedBy =
                actor;

        this.decidedAt =
                Instant.now();
    }


    public void reopen() {

        this.outcome =
                BidDecisionOutcome.PENDING;

        this.decidedBy =
                null;

        this.decidedAt =
                null;
    }


    @PrePersist
    void beforeInsert() {

        Instant now =
                Instant.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
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


    public BidDecisionOutcome getOutcome() {
        return outcome;
    }


    public BidDecisionOutcome getRecommendedOutcome() {
        return recommendedOutcome;
    }


    public int getDecisionScore() {
        return decisionScore;
    }


    public int getMatchScoreSnapshot() {
        return matchScoreSnapshot;
    }


    public TenderMatchGrade getMatchGradeSnapshot() {
        return matchGradeSnapshot;
    }


    public int getStrategicFit() {
        return strategicFit;
    }


    public int getCapabilityConfidence() {
        return capabilityConfidence;
    }


    public int getCommercialAttractiveness() {
        return commercialAttractiveness;
    }


    public int getDeliveryFeasibility() {
        return deliveryFeasibility;
    }


    public int getResourceCapacity() {
        return resourceCapacity;
    }


    public int getRiskConfidence() {
        return riskConfidence;
    }


    public String getRecommendationReason() {
        return recommendationReason;
    }


    public String getRationale() {
        return rationale;
    }


    public String getKeyRisks() {
        return keyRisks;
    }


    public String getAssumptions() {
        return assumptions;
    }


    public String getDecidedBy() {
        return decidedBy;
    }


    public Instant getDecidedAt() {
        return decidedAt;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }


    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
