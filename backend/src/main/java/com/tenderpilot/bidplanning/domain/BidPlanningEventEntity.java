package com.tenderpilot.bidplanning.domain;

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
import jakarta.persistence.Table;

import java.time.Instant;

import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_planning_events"
)
public class BidPlanningEventEntity {

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
            name = "event_type",
            nullable = false,
            length = 60
    )
    private BidPlanningEventType eventType;


    @Column(
            name = "task_id"
    )
    private UUID taskId;


    @Column(
            name = "actor",
            nullable = false,
            length = 320
    )
    private String actor;


    @Column(
            name = "detail",
            nullable = false,
            length = 2000
    )
    private String detail;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected BidPlanningEventEntity() {
    }


    private BidPlanningEventEntity(
            UUID organizationId,
            TenderEntity tender,
            BidPlanningEventType eventType,
            UUID taskId,
            String actor,
            String detail
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.eventType =
                eventType;

        this.taskId =
                taskId;

        this.actor =
                actor;

        this.detail =
                detail;
    }


    public static BidPlanningEventEntity create(
            UUID organizationId,
            TenderEntity tender,
            BidPlanningEventType eventType,
            UUID taskId,
            String actor,
            String detail
    ) {

        return new BidPlanningEventEntity(
                organizationId,
                tender,
                eventType,
                taskId,
                actor,
                detail
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


    public BidPlanningEventType getEventType() {
        return eventType;
    }


    public UUID getTaskId() {
        return taskId;
    }


    public String getActor() {
        return actor;
    }


    public String getDetail() {
        return detail;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }

}
