package com.tenderpilot.search.application;

import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.procurement.award.domain.TenderAwardEntity;
import com.tenderpilot.procurement.award.infrastructure.TenderAwardRepository;

import com.tenderpilot.procurement.domain.TenderEntity;
import com.tenderpilot.procurement.infrastructure.TenderRepository;

import com.tenderpilot.procurement.quality.domain.ProcurementDataQualityAssessmentEntity;

import com.tenderpilot.procurement.quality.infrastructure.ProcurementDataQualityAssessmentRepository;

import com.tenderpilot.search.domain.ProcurementSearchDocument;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcurementSearchDocumentBuilder {

    private final TenderRepository
            tenderRepository;


    private final TenderAwardRepository
            awardRepository;


    private final ProcurementDataQualityAssessmentRepository
            qualityRepository;


    public ProcurementSearchDocumentBuilder(

            TenderRepository tenderRepository,

            TenderAwardRepository awardRepository,

            ProcurementDataQualityAssessmentRepository qualityRepository

    ) {

        this.tenderRepository =
                tenderRepository;

        this.awardRepository =
                awardRepository;

        this.qualityRepository =
                qualityRepository;

    }


    @Transactional(
            readOnly = true
    )
    public ProcurementSearchDocument build(
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
                                                "Tender was not found for search projection"
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


        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElse(
                                null
                        );


        return new ProcurementSearchDocument(

                tender.getId(),

                tender.getCanonicalKey(),

                tender.getTitle(),

                tender.getDescription(),

                tender
                        .getBuyer()
                        .getLegalName(),

                tender
                        .getBuyer()
                        .getCategory()
                        .name(),

                tender
                        .getStatus()
                        .name(),

                tender
                        .getProcedureType()
                        .name(),

                tender
                        .getContractNature()
                        .name(),

                tender.getPublicationDate()
                        ==
                        null
                        ? null
                        : tender
                                .getPublicationDate()
                                .toString(),

                tender.getSubmissionDeadline()
                        ==
                        null
                        ? null
                        : tender
                                .getSubmissionDeadline()
                                .toString(),

                tender.getEstimatedMinValue(),

                tender.getEstimatedMaxValue(),

                tender.getCurrency(),

                tender.getCountryCode(),

                tender.getPrimaryRegionCode(),

                tender.getPrimaryRegionName(),

                tender.isFrameworkAgreement(),

                tender.isJointProcurement(),

                tender.isElectronicSubmission(),

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

                tender
                        .getSourceRecords()
                        .stream()
                        .map(
                                source ->
                                        source
                                                .getSource()
                                                .name()
                        )
                        .distinct()
                        .toList(),

                tender
                        .getLots()
                        .stream()
                        .map(
                                lot ->
                                        lot.getTitle()
                        )
                        .filter(
                                value ->
                                        value != null
                        )
                        .distinct()
                        .toList(),

                award == null
                        ? null
                        : award.getAwardedAmount(),

                award == null
                        ? null
                        : award.getCurrency(),

                award == null
                        ? List.of()
                        : award
                                .getSuppliers()
                                .stream()
                                .map(
                                        supplier ->
                                                supplier.getLegalName()
                                )
                                .filter(
                                        value ->
                                                value != null
                                )
                                .distinct()
                                .toList(),

                award == null
                        ? List.of()
                        : award
                                .getSuppliers()
                                .stream()
                                .map(
                                        supplier ->
                                                supplier.getIdentifier()
                                )
                                .filter(
                                        value ->
                                                value != null
                                )
                                .distinct()
                                .toList(),

                quality == null
                        ? null
                        : quality.getScore(),

                quality == null
                        ? null
                        : quality
                                .getGrade()
                                .name(),

                quality == null
                        ? List.of()
                        : quality
                                .getIssues()
                                .stream()
                                .map(
                                        issue ->
                                                issue
                                                        .getCode()
                                                        .name()
                                )
                                .distinct()
                                .toList()

        );

    }

}
