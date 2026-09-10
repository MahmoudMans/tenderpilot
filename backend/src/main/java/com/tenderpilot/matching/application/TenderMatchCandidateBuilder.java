package com.tenderpilot.matching.application;

import com.tenderpilot.matching.domain.TenderMatchCandidate;

import com.tenderpilot.procurement.domain.TenderEntity;

import com.tenderpilot.procurement.quality.domain.ProcurementDataQualityAssessmentEntity;

import com.tenderpilot.procurement.quality.infrastructure.ProcurementDataQualityAssessmentRepository;

import org.springframework.stereotype.Component;

@Component
public class TenderMatchCandidateBuilder {

    private final ProcurementDataQualityAssessmentRepository
            qualityRepository;


    public TenderMatchCandidateBuilder(
            ProcurementDataQualityAssessmentRepository qualityRepository
    ) {

        this.qualityRepository =
                qualityRepository;
    }


    public TenderMatchCandidate build(
            TenderEntity tender
    ) {

        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tender.getId()
                        )
                        .orElse(
                                null
                        );


        return new TenderMatchCandidate(
                tender.getId(),

                tender.getTitle(),

                tender.getDescription(),

                tender
                        .getBuyer()
                        .getCategory()
                        .name(),

                tender.getCountryCode(),

                tender.getPrimaryRegionCode(),

                tender.getPrimaryRegionName(),

                tender.getEstimatedMinValue(),

                tender.getEstimatedMaxValue(),

                tender.getCurrency(),

                tender.getSubmissionDeadline(),

                tender
                        .getCpvs()
                        .stream()
                        .map(
                                cpv ->
                                        cpv.getCode()
                        )
                        .distinct()
                        .toList(),

                tender
                        .getCpvs()
                        .stream()
                        .map(
                                cpv ->
                                        cpv.getLabel()
                        )
                        .distinct()
                        .toList(),

                quality == null
                        ? null
                        : quality.getScore()
        );
    }

}
