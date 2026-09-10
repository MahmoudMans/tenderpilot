package com.tenderpilot.bidplanning.domain;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "organization_bid_submission_controls",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bid_submission_control_org_tender",
                columnNames = {
                        "organization_id",
                        "tender_id"
                }
        )
)
public class BidSubmissionControlEntity {

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
            name = "portal_ready",
            nullable = false
    )
    private boolean portalReady;


    @Column(
            name = "signatures_ready",
            nullable = false
    )
    private boolean signaturesReady;


    @Column(
            name = "technical_response_ready",
            nullable = false
    )
    private boolean technicalResponseReady;


    @Column(
            name = "commercial_response_ready",
            nullable = false
    )
    private boolean commercialResponseReady;


    @Column(
            name = "administrative_pack_ready",
            nullable = false
    )
    private boolean administrativePackReady;


    @Column(
            name = "final_review_ready",
            nullable = false
    )
    private boolean finalReviewReady;


    @Column(
            name = "submission_authorized",
            nullable = false
    )
    private boolean submissionAuthorized;


    @Column(
            name = "planned_submission_at"
    )
    private Instant plannedSubmissionAt;


    @Column(
            name = "submitted_at"
    )
    private Instant submittedAt;


    @Column(
            name = "submission_reference",
            length = 500
    )
    private String submissionReference;


    @Column(
            name = "submitted_by",
            length = 320
    )
    private String submittedBy;


    @Column(
            name = "notes",
            length = 3000
    )
    private String notes;


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


    protected BidSubmissionControlEntity() {
    }


    private BidSubmissionControlEntity(
            UUID organizationId,
            TenderEntity tender,
            String actor
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.updatedBy =
                actor;
    }


    public static BidSubmissionControlEntity create(
            UUID organizationId,
            TenderEntity tender,
            String actor
    ) {

        return new BidSubmissionControlEntity(
                organizationId,
                tender,
                actor
        );
    }


    public void updateChecklist(
            boolean portalReady,
            boolean signaturesReady,
            boolean technicalResponseReady,
            boolean commercialResponseReady,
            boolean administrativePackReady,
            boolean finalReviewReady,
            Instant plannedSubmissionAt,
            String notes,
            String actor
    ) {

        this.portalReady =
                portalReady;

        this.signaturesReady =
                signaturesReady;

        this.technicalResponseReady =
                technicalResponseReady;

        this.commercialResponseReady =
                commercialResponseReady;

        this.administrativePackReady =
                administrativePackReady;

        this.finalReviewReady =
                finalReviewReady;

        this.plannedSubmissionAt =
                plannedSubmissionAt;

        this.notes =
                notes;

        this.updatedBy =
                actor;
    }


    public void authorize(
            boolean authorized,
            String actor
    ) {

        this.submissionAuthorized =
                authorized;

        this.updatedBy =
                actor;
    }


    public void markSubmitted(
            String reference,
            String notes,
            String actor
    ) {

        this.submittedAt =
                Instant.now();

        this.submissionReference =
                reference;

        this.submittedBy =
                actor;

        if (
                notes != null
        ) {

            this.notes =
                    notes;
        }


        this.updatedBy =
                actor;
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


    public boolean isPortalReady() {
        return portalReady;
    }


    public boolean isSignaturesReady() {
        return signaturesReady;
    }


    public boolean isTechnicalResponseReady() {
        return technicalResponseReady;
    }


    public boolean isCommercialResponseReady() {
        return commercialResponseReady;
    }


    public boolean isAdministrativePackReady() {
        return administrativePackReady;
    }


    public boolean isFinalReviewReady() {
        return finalReviewReady;
    }


    public boolean isSubmissionAuthorized() {
        return submissionAuthorized;
    }


    public Instant getPlannedSubmissionAt() {
        return plannedSubmissionAt;
    }


    public Instant getSubmittedAt() {
        return submittedAt;
    }


    public String getSubmissionReference() {
        return submissionReference;
    }


    public String getSubmittedBy() {
        return submittedBy;
    }


    public String getNotes() {
        return notes;
    }


    public String getUpdatedBy() {
        return updatedBy;
    }

}
