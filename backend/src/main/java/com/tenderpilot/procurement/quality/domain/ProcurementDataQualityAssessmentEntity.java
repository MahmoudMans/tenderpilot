package com.tenderpilot.procurement.quality.domain;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "procurement_data_quality_assessments"
)
public class ProcurementDataQualityAssessmentEntity {

    @Id
    private UUID id;


    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "tender_id",
            nullable = false,
            unique = true
    )
    private TenderEntity tender;


    @Column(
            name = "score",
            nullable = false
    )
    private int score;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "grade",
            nullable = false,
            length = 40
    )
    private DataQualityGrade grade;


    @Column(
            name = "critical_issue_count",
            nullable = false
    )
    private int criticalIssueCount;


    @Column(
            name = "warning_issue_count",
            nullable = false
    )
    private int warningIssueCount;


    @Column(
            name = "info_issue_count",
            nullable = false
    )
    private int infoIssueCount;


    @Column(
            name = "assessed_at",
            nullable = false
    )
    private Instant assessedAt;


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "procurement_data_quality_issues",
            joinColumns = @JoinColumn(
                    name = "assessment_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<DataQualityIssueValue> issues =
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


    protected ProcurementDataQualityAssessmentEntity() {
    }


    private ProcurementDataQualityAssessmentEntity(
            TenderEntity tender
    ) {

        this.id = UUID.randomUUID();
        this.tender = tender;
    }


    public static ProcurementDataQualityAssessmentEntity create(
            TenderEntity tender
    ) {

        return new ProcurementDataQualityAssessmentEntity(
                tender
        );
    }


    public void apply(
            DataQualityEvaluation evaluation
    ) {

        this.score =
                evaluation.score();

        this.grade =
                evaluation.grade();

        this.issues.clear();

        this.issues.addAll(
                evaluation.issues()
        );

        this.criticalIssueCount =
                (int)
                        evaluation
                                .issues()
                                .stream()
                                .filter(
                                        issue ->
                                                issue.getSeverity()
                                                        ==
                                                        DataQualitySeverity.CRITICAL
                                )
                                .count();

        this.warningIssueCount =
                (int)
                        evaluation
                                .issues()
                                .stream()
                                .filter(
                                        issue ->
                                                issue.getSeverity()
                                                        ==
                                                        DataQualitySeverity.WARNING
                                )
                                .count();

        this.infoIssueCount =
                (int)
                        evaluation
                                .issues()
                                .stream()
                                .filter(
                                        issue ->
                                                issue.getSeverity()
                                                        ==
                                                        DataQualitySeverity.INFO
                                )
                                .count();

        this.assessedAt =
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

        if (assessedAt == null) {
            assessedAt = now;
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


    public TenderEntity getTender() {
        return tender;
    }


    public int getScore() {
        return score;
    }


    public DataQualityGrade getGrade() {
        return grade;
    }


    public int getCriticalIssueCount() {
        return criticalIssueCount;
    }


    public int getWarningIssueCount() {
        return warningIssueCount;
    }


    public int getInfoIssueCount() {
        return infoIssueCount;
    }


    public int getIssueCount() {
        return criticalIssueCount
                +
                warningIssueCount
                +
                infoIssueCount;
    }


    public Instant getAssessedAt() {
        return assessedAt;
    }


    public List<DataQualityIssueValue> getIssues() {

        return List.copyOf(
                issues
        );
    }


    public Instant getCreatedAt() {
        return createdAt;
    }


    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
