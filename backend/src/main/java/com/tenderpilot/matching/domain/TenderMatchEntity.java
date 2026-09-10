package com.tenderpilot.matching.domain;

import com.tenderpilot.procurement.domain.TenderEntity;

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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "organization_tender_matches",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_org_tender_match",
                columnNames = {
                        "organization_id",
                        "tender_id"
                }
        )
)
public class TenderMatchEntity {

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


    @Column(
            name = "total_score",
            nullable = false
    )
    private int totalScore;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "grade",
            nullable = false,
            length = 30
    )
    private TenderMatchGrade grade;


    @Column(
            name = "blocked",
            nullable = false
    )
    private boolean blocked;


    @Column(name = "cpv_score", nullable = false)
    private int cpvScore;


    @Column(name = "geography_score", nullable = false)
    private int geographyScore;


    @Column(name = "value_score", nullable = false)
    private int valueScore;


    @Column(name = "buyer_score", nullable = false)
    private int buyerScore;


    @Column(name = "capability_score", nullable = false)
    private int capabilityScore;


    @Column(name = "timing_score", nullable = false)
    private int timingScore;


    @Column(name = "quality_score", nullable = false)
    private int qualityScore;


    @Column(
            name = "evaluated_at",
            nullable = false
    )
    private Instant evaluatedAt;


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "organization_tender_match_signals",
            joinColumns = @JoinColumn(
                    name = "match_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<MatchSignalValue> signals =
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


    protected TenderMatchEntity() {
    }


    private TenderMatchEntity(
            UUID organizationId,
            TenderEntity tender
    ) {

        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.tender = tender;
    }


    public static TenderMatchEntity create(
            UUID organizationId,
            TenderEntity tender
    ) {

        return new TenderMatchEntity(
                organizationId,
                tender
        );
    }


    public void apply(
            TenderMatchEvaluation evaluation
    ) {

        this.totalScore =
                evaluation.totalScore();

        this.grade =
                evaluation.grade();

        this.blocked =
                evaluation.blocked();

        this.cpvScore =
                evaluation.cpvScore();

        this.geographyScore =
                evaluation.geographyScore();

        this.valueScore =
                evaluation.valueScore();

        this.buyerScore =
                evaluation.buyerScore();

        this.capabilityScore =
                evaluation.capabilityScore();

        this.timingScore =
                evaluation.timingScore();

        this.qualityScore =
                evaluation.qualityScore();

        this.signals.clear();

        this.signals.addAll(
                evaluation.signals()
        );

        this.evaluatedAt =
                Instant.now();
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

        if (evaluatedAt == null) {
            evaluatedAt = now;
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


    public int getTotalScore() {
        return totalScore;
    }


    public TenderMatchGrade getGrade() {
        return grade;
    }


    public boolean isBlocked() {
        return blocked;
    }


    public int getCpvScore() {
        return cpvScore;
    }


    public int getGeographyScore() {
        return geographyScore;
    }


    public int getValueScore() {
        return valueScore;
    }


    public int getBuyerScore() {
        return buyerScore;
    }


    public int getCapabilityScore() {
        return capabilityScore;
    }


    public int getTimingScore() {
        return timingScore;
    }


    public int getQualityScore() {
        return qualityScore;
    }


    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }


    public List<MatchSignalValue> getSignals() {

        return List.copyOf(
                signals
        );
    }

}
