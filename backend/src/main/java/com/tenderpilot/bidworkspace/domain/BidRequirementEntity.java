package com.tenderpilot.bidworkspace.domain;

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
        name = "organization_bid_requirements"
)
public class BidRequirementEntity {

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
            name = "source_document_id"
    )
    private BidDocumentEntity sourceDocument;


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
            name = "requirement_category",
            nullable = false,
            length = 50
    )
    private BidRequirementCategory category;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 50
    )
    private BidRequirementStatus status;


    @Column(
            name = "mandatory",
            nullable = false
    )
    private boolean mandatory;


    @Column(
            name = "source_reference",
            length = 500
    )
    private String sourceReference;


    @Column(
            name = "due_date"
    )
    private LocalDate dueDate;


    @Column(
            name = "assigned_to",
            length = 320
    )
    private String assignedTo;


    @Column(
            name = "evidence_notes",
            length = 3000
    )
    private String evidenceNotes;


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


    protected BidRequirementEntity() {
    }


    private BidRequirementEntity(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentEntity sourceDocument,
            String title,
            String description,
            BidRequirementCategory category,
            boolean mandatory,
            String sourceReference,
            LocalDate dueDate,
            String assignedTo,
            String evidenceNotes,
            String actor
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.sourceDocument =
                sourceDocument;

        this.title =
                title;

        this.description =
                description;

        this.category =
                category;

        this.status =
                BidRequirementStatus.NOT_REVIEWED;

        this.mandatory =
                mandatory;

        this.sourceReference =
                sourceReference;

        this.dueDate =
                dueDate;

        this.assignedTo =
                assignedTo;

        this.evidenceNotes =
                evidenceNotes;

        this.createdBy =
                actor;

        this.updatedBy =
                actor;
    }


    public static BidRequirementEntity create(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentEntity sourceDocument,
            String title,
            String description,
            BidRequirementCategory category,
            boolean mandatory,
            String sourceReference,
            LocalDate dueDate,
            String assignedTo,
            String evidenceNotes,
            String actor
    ) {

        return new BidRequirementEntity(
                organizationId,
                tender,
                sourceDocument,
                title,
                description,
                category,
                mandatory,
                sourceReference,
                dueDate,
                assignedTo,
                evidenceNotes,
                actor
        );
    }


    public void update(
            BidDocumentEntity sourceDocument,
            String title,
            String description,
            BidRequirementCategory category,
            BidRequirementStatus status,
            boolean mandatory,
            String sourceReference,
            LocalDate dueDate,
            String assignedTo,
            String evidenceNotes,
            String actor
    ) {

        this.sourceDocument =
                sourceDocument;

        this.title =
                title;

        this.description =
                description;

        this.category =
                category;

        this.status =
                status;

        this.mandatory =
                mandatory;

        this.sourceReference =
                sourceReference;

        this.dueDate =
                dueDate;

        this.assignedTo =
                assignedTo;

        this.evidenceNotes =
                evidenceNotes;

        this.updatedBy =
                actor;
    }


    public void detachSourceDocument() {

        this.sourceDocument =
                null;
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


    public BidDocumentEntity getSourceDocument() {
        return sourceDocument;
    }


    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }


    public BidRequirementCategory getCategory() {
        return category;
    }


    public BidRequirementStatus getStatus() {
        return status;
    }


    public boolean isMandatory() {
        return mandatory;
    }


    public String getSourceReference() {
        return sourceReference;
    }


    public LocalDate getDueDate() {
        return dueDate;
    }


    public String getAssignedTo() {
        return assignedTo;
    }


    public String getEvidenceNotes() {
        return evidenceNotes;
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
