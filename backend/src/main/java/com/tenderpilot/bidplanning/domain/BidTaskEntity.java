package com.tenderpilot.bidplanning.domain;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_tasks"
)
public class BidTaskEntity {

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


    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "requirement_id"
    )
    private BidRequirementEntity requirement;


    @Column(
            name = "title",
            nullable = false,
            length = 600
    )
    private String title;


    @Column(
            name = "description",
            length = 3000
    )
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private BidTaskStatus status;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "priority",
            nullable = false,
            length = 40
    )
    private BidTaskPriority priority;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "milestone",
            nullable = false,
            length = 60
    )
    private BidTaskMilestone milestone;


    @Column(
            name = "assigned_to",
            length = 320
    )
    private String assignedTo;


    @Column(
            name = "due_date"
    )
    private LocalDate dueDate;


    @Column(
            name = "completed_at"
    )
    private Instant completedAt;


    @Column(
            name = "created_by",
            nullable = false,
            length = 320
    )
    private String createdBy;


    @Column(
            name = "updated_by",
            nullable = false,
            length = 320
    )
    private String updatedBy;


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


    protected BidTaskEntity() {
    }


    private BidTaskEntity(
            UUID organizationId,
            TenderEntity tender,
            BidRequirementEntity requirement,
            String title,
            String description,
            BidTaskPriority priority,
            BidTaskMilestone milestone,
            String assignedTo,
            LocalDate dueDate,
            String actor
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.requirement =
                requirement;

        this.title =
                title;

        this.description =
                description;

        this.status =
                BidTaskStatus.TODO;

        this.priority =
                priority;

        this.milestone =
                milestone;

        this.assignedTo =
                assignedTo;

        this.dueDate =
                dueDate;

        this.createdBy =
                actor;

        this.updatedBy =
                actor;
    }


    public static BidTaskEntity create(
            UUID organizationId,
            TenderEntity tender,
            BidRequirementEntity requirement,
            String title,
            String description,
            BidTaskPriority priority,
            BidTaskMilestone milestone,
            String assignedTo,
            LocalDate dueDate,
            String actor
    ) {

        return new BidTaskEntity(
                organizationId,
                tender,
                requirement,
                title,
                description,
                priority,
                milestone,
                assignedTo,
                dueDate,
                actor
        );
    }


    public void update(
            BidRequirementEntity requirement,
            String title,
            String description,
            BidTaskStatus status,
            BidTaskPriority priority,
            BidTaskMilestone milestone,
            String assignedTo,
            LocalDate dueDate,
            String actor
    ) {

        this.requirement =
                requirement;

        this.title =
                title;

        this.description =
                description;

        this.status =
                status;

        this.priority =
                priority;

        this.milestone =
                milestone;

        this.assignedTo =
                assignedTo;

        this.dueDate =
                dueDate;

        this.updatedBy =
                actor;


        if (
                status
                        ==
                        BidTaskStatus.DONE
        ) {

            if (
                    completedAt == null
            ) {

                completedAt =
                        Instant.now();
            }


        } else {

            completedAt =
                    null;
        }
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


    public UUID getOrganizationId() {
        return organizationId;
    }


    public TenderEntity getTender() {
        return tender;
    }


    public BidRequirementEntity getRequirement() {
        return requirement;
    }


    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }


    public BidTaskStatus getStatus() {
        return status;
    }


    public BidTaskPriority getPriority() {
        return priority;
    }


    public BidTaskMilestone getMilestone() {
        return milestone;
    }


    public String getAssignedTo() {
        return assignedTo;
    }


    public LocalDate getDueDate() {
        return dueDate;
    }


    public Instant getCompletedAt() {
        return completedAt;
    }


    public String getCreatedBy() {
        return createdBy;
    }


    public String getUpdatedBy() {
        return updatedBy;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }


    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
