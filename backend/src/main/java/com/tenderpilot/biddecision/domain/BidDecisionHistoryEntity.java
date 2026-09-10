package com.tenderpilot.biddecision.domain;

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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_decision_history"
)
public class BidDecisionHistoryEntity {

    @Id
    private UUID id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "decision_id",
            nullable = false
    )
    private BidDecisionEntity decision;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 40
    )
    private BidDecisionEventType eventType;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "previous_outcome",
            length = 30
    )
    private BidDecisionOutcome previousOutcome;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "new_outcome",
            nullable = false,
            length = 30
    )
    private BidDecisionOutcome newOutcome;


    @Column(
            name = "decision_score",
            nullable = false
    )
    private int decisionScore;


    @Column(
            name = "actor",
            nullable = false,
            length = 320
    )
    private String actor;


    @Column(
            name = "note",
            length = 2000
    )
    private String note;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected BidDecisionHistoryEntity() {
    }


    private BidDecisionHistoryEntity(
            BidDecisionEntity decision,
            BidDecisionEventType eventType,
            BidDecisionOutcome previousOutcome,
            BidDecisionOutcome newOutcome,
            int decisionScore,
            String actor,
            String note
    ) {

        this.id =
                UUID.randomUUID();

        this.decision =
                decision;

        this.eventType =
                eventType;

        this.previousOutcome =
                previousOutcome;

        this.newOutcome =
                newOutcome;

        this.decisionScore =
                decisionScore;

        this.actor =
                actor;

        this.note =
                note;
    }


    public static BidDecisionHistoryEntity create(
            BidDecisionEntity decision,
            BidDecisionEventType eventType,
            BidDecisionOutcome previousOutcome,
            BidDecisionOutcome newOutcome,
            int decisionScore,
            String actor,
            String note
    ) {

        return new BidDecisionHistoryEntity(
                decision,
                eventType,
                previousOutcome,
                newOutcome,
                decisionScore,
                actor,
                note
        );
    }


    @PrePersist
    void beforeInsert() {

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }


    public UUID getId() {
        return id;
    }


    public BidDecisionEntity getDecision() {
        return decision;
    }


    public BidDecisionEventType getEventType() {
        return eventType;
    }


    public BidDecisionOutcome getPreviousOutcome() {
        return previousOutcome;
    }


    public BidDecisionOutcome getNewOutcome() {
        return newOutcome;
    }


    public int getDecisionScore() {
        return decisionScore;
    }


    public String getActor() {
        return actor;
    }


    public String getNote() {
        return note;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }

}
