package com.tenderpilot.procurement.award.domain;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "procurement_tender_awards")
public class TenderAwardEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tender_id",
            nullable = false,
            unique = true
    )
    private TenderEntity tender;

    @Column(name = "notification_date")
    private LocalDate notificationDate;

    @Column(
            name = "awarded_amount",
            precision = 19,
            scale = 2
    )
    private BigDecimal awardedAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "price_form", length = 160)
    private String priceForm;

    @Column(name = "offers_received")
    private Integer offersReceived;

    @Column(name = "framework_agreement_id", length = 220)
    private String frameworkAgreementId;

    @Column(name = "social_considerations", length = 600)
    private String socialConsiderations;

    @Column(name = "environmental_considerations", length = 600)
    private String environmentalConsiderations;

    @Column(name = "execution_modalities", length = 600)
    private String executionModalities;

    @Column(name = "operator_group_type", length = 160)
    private String operatorGroupType;

    @Column(name = "subcontracting_declared", length = 160)
    private String subcontractingDeclared;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "procurement_tender_award_suppliers",
            joinColumns = @JoinColumn(name = "award_id")
    )
    @OrderColumn(name = "position")
    private List<AwardSupplierValue> suppliers =
            new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TenderAwardEntity() {
    }

    private TenderAwardEntity(TenderEntity tender) {
        this.id = UUID.randomUUID();
        this.tender = tender;
    }

    public static TenderAwardEntity create(
            TenderEntity tender
    ) {
        return new TenderAwardEntity(tender);
    }

    public void update(
            LocalDate notificationDate,
            BigDecimal awardedAmount,
            String currency,
            Integer durationMonths,
            String priceForm,
            Integer offersReceived,
            String frameworkAgreementId,
            String socialConsiderations,
            String environmentalConsiderations,
            String executionModalities,
            String operatorGroupType,
            String subcontractingDeclared
    ) {
        this.notificationDate = notificationDate;
        this.awardedAmount = awardedAmount;
        this.currency = currency;
        this.durationMonths = durationMonths;
        this.priceForm = priceForm;
        this.offersReceived = offersReceived;
        this.frameworkAgreementId = frameworkAgreementId;
        this.socialConsiderations = socialConsiderations;
        this.environmentalConsiderations =
                environmentalConsiderations;
        this.executionModalities = executionModalities;
        this.operatorGroupType = operatorGroupType;
        this.subcontractingDeclared = subcontractingDeclared;
    }

    public void replaceSuppliers(
            List<AwardSupplierValue> values
    ) {
        suppliers.clear();
        suppliers.addAll(values);
    }

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();

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
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public TenderEntity getTender() {
        return tender;
    }

    public LocalDate getNotificationDate() {
        return notificationDate;
    }

    public BigDecimal getAwardedAmount() {
        return awardedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public String getPriceForm() {
        return priceForm;
    }

    public Integer getOffersReceived() {
        return offersReceived;
    }

    public String getFrameworkAgreementId() {
        return frameworkAgreementId;
    }

    public String getSocialConsiderations() {
        return socialConsiderations;
    }

    public String getEnvironmentalConsiderations() {
        return environmentalConsiderations;
    }

    public String getExecutionModalities() {
        return executionModalities;
    }

    public String getOperatorGroupType() {
        return operatorGroupType;
    }

    public String getSubcontractingDeclared() {
        return subcontractingDeclared;
    }

    public List<AwardSupplierValue> getSuppliers() {
        return List.copyOf(suppliers);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
