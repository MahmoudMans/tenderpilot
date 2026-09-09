package com.tenderpilot.procurement.quality.application;

import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.procurement.award.domain.TenderAwardEntity;
import com.tenderpilot.procurement.award.infrastructure.TenderAwardRepository;

import com.tenderpilot.procurement.domain.TenderEntity;
import com.tenderpilot.procurement.infrastructure.TenderRepository;

import com.tenderpilot.procurement.quality.api.DataQualityOverviewResponse;

import com.tenderpilot.procurement.quality.domain.DataQualityEvaluation;
import com.tenderpilot.procurement.quality.domain.DataQualityGrade;
import com.tenderpilot.procurement.quality.domain.ProcurementDataQualityAssessmentEntity;

import com.tenderpilot.procurement.quality.infrastructure.ProcurementDataQualityAssessmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcurementDataQualityService {

    private final TenderRepository
            tenderRepository;


    private final TenderAwardRepository
            awardRepository;


    private final ProcurementDataQualityAssessmentRepository
            assessmentRepository;


    private final ProcurementDataQualityEvaluator
            evaluator;


    public ProcurementDataQualityService(
            TenderRepository tenderRepository,
            TenderAwardRepository awardRepository,
            ProcurementDataQualityAssessmentRepository
                    assessmentRepository,
            ProcurementDataQualityEvaluator evaluator
    ) {

        this.tenderRepository =
                tenderRepository;

        this.awardRepository =
                awardRepository;

        this.assessmentRepository =
                assessmentRepository;

        this.evaluator =
                evaluator;
    }


    @Transactional
    public ProcurementDataQualityAssessmentEntity
    assessAndPersist(
            UUID tenderId
    ) {

        TenderEntity tender =
                tenderRepository
                        .findById(
                                tenderId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Tender was not found for data quality assessment"
                                        )
                        );


        TenderAwardEntity award =
                awardRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElse(
                                null
                        );


        DataQualityEvaluation evaluation =
                evaluator.evaluate(
                        tender,
                        award
                );


        ProcurementDataQualityAssessmentEntity
                assessment =
                assessmentRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElseGet(
                                () ->
                                        ProcurementDataQualityAssessmentEntity
                                                .create(
                                                        tender
                                                )
                        );


        assessment.apply(
                evaluation
        );


        return assessmentRepository
                .saveAndFlush(
                        assessment
                );
    }


    @Transactional
    public int recalculateAll() {

        List<TenderEntity> tenders =
                tenderRepository.findAll();


        for (
                TenderEntity tender :
                tenders
        ) {

            assessAndPersist(
                    tender.getId()
            );
        }


        return tenders.size();
    }


    @Transactional(readOnly = true)
    public DataQualityOverviewResponse overview() {

        long totalTenders =
                tenderRepository.count();


        List<ProcurementDataQualityAssessmentEntity>
                assessments =
                assessmentRepository.findAll();


        long assessed =
                assessments.size();


        double average =
                assessments.isEmpty()
                        ? 0.0
                        : assessments
                                .stream()
                                .mapToInt(
                                        ProcurementDataQualityAssessmentEntity
                                                ::getScore
                                )
                                .average()
                                .orElse(
                                        0.0
                                );


        long excellent =
                countGrade(
                        assessments,
                        DataQualityGrade.EXCELLENT
                );


        long good =
                countGrade(
                        assessments,
                        DataQualityGrade.GOOD
                );


        long fair =
                countGrade(
                        assessments,
                        DataQualityGrade.FAIR
                );


        long poor =
                countGrade(
                        assessments,
                        DataQualityGrade.POOR
                );


        long critical =
                countGrade(
                        assessments,
                        DataQualityGrade.CRITICAL
                );


        long criticalIssues =
                assessments
                        .stream()
                        .mapToLong(
                                ProcurementDataQualityAssessmentEntity
                                        ::getCriticalIssueCount
                        )
                        .sum();


        long warningIssues =
                assessments
                        .stream()
                        .mapToLong(
                                ProcurementDataQualityAssessmentEntity
                                        ::getWarningIssueCount
                        )
                        .sum();


        long infoIssues =
                assessments
                        .stream()
                        .mapToLong(
                                ProcurementDataQualityAssessmentEntity
                                        ::getInfoIssueCount
                        )
                        .sum();


        Instant lastAssessedAt =
                assessments
                        .stream()
                        .map(
                                ProcurementDataQualityAssessmentEntity
                                        ::getAssessedAt
                        )
                        .max(
                                Instant::compareTo
                        )
                        .orElse(
                                null
                        );


        return new DataQualityOverviewResponse(
                totalTenders,
                assessed,
                Math.max(
                        0,
                        totalTenders - assessed
                ),
                average,
                excellent,
                good,
                fair,
                poor,
                critical,
                criticalIssues,
                warningIssues,
                infoIssues,
                lastAssessedAt
        );
    }


    private long countGrade(
            List<ProcurementDataQualityAssessmentEntity>
                    assessments,
            DataQualityGrade grade
    ) {

        return assessments
                .stream()
                .filter(
                        assessment ->
                                assessment.getGrade()
                                        ==
                                        grade
                )
                .count();
    }

}
