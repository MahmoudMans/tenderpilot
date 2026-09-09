package com.tenderpilot.procurement.application;

import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.procurement.api.OpportunityDetailResponse;
import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import com.tenderpilot.procurement.award.domain.TenderAwardEntity;
import com.tenderpilot.procurement.award.infrastructure.TenderAwardRepository;

import com.tenderpilot.procurement.domain.CpvClassificationValue;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;
import com.tenderpilot.procurement.domain.TenderEntity;

import com.tenderpilot.procurement.infrastructure.TenderRepository;

import com.tenderpilot.procurement.quality.domain.DataQualityGrade;
import com.tenderpilot.procurement.quality.domain.ProcurementDataQualityAssessmentEntity;

import com.tenderpilot.procurement.quality.infrastructure.ProcurementDataQualityAssessmentRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpportunityQueryService {

    private final TenderRepository
            tenderRepository;


    private final TenderAwardRepository
            awardRepository;


    private final ProcurementDataQualityAssessmentRepository
            qualityRepository;


    public OpportunityQueryService(
            TenderRepository tenderRepository,
            TenderAwardRepository awardRepository,
            ProcurementDataQualityAssessmentRepository
                    qualityRepository
    ) {

        this.tenderRepository =
                tenderRepository;

        this.awardRepository =
                awardRepository;

        this.qualityRepository =
                qualityRepository;
    }


    @Transactional(readOnly = true)
    public List<OpportunitySummaryResponse> list(
            String query,
            ProcurementStatus status,
            ProcurementSource source
    ) {

        return list(
                query,
                status,
                source,
                null,
                null
        );
    }


    @Transactional(readOnly = true)
    public List<OpportunitySummaryResponse> list(
            String query,
            ProcurementStatus status,
            ProcurementSource source,
            Integer minQualityScore,
            DataQualityGrade qualityGrade
    ) {

        String normalizedQuery =
                query == null
                        ? null
                        : query
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );


        return tenderRepository
                .findTop100ByOrderByPublicationDateDesc()
                .stream()

                .filter(
                        tender ->
                                status == null
                                ||
                                tender.getStatus()
                                        ==
                                        status
                )

                .filter(
                        tender ->
                                source == null
                                ||
                                tender
                                        .getSourceRecords()
                                        .stream()
                                        .anyMatch(
                                                record ->
                                                        record.getSource()
                                                                ==
                                                                source
                                        )
                )

                .filter(
                        tender ->
                                matchesText(
                                        tender,
                                        normalizedQuery
                                )
                )

                .filter(
                        tender ->
                                matchesQuality(
                                        tender,
                                        minQualityScore,
                                        qualityGrade
                                )
                )

                .map(
                        this::summary
                )

                .toList();
    }


    @Transactional(readOnly = true)
    public OpportunityDetailResponse detail(
            UUID id
    ) {

        TenderEntity tender =
                tenderRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Opportunity was not found"
                                        )
                        );


        return detailResponse(
                tender
        );
    }


    private boolean matchesText(
            TenderEntity tender,
            String query
    ) {

        if (
                query == null
                ||
                query.isBlank()
        ) {
            return true;
        }


        if (
                tender.getTitle()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .contains(
                                query
                        )
        ) {
            return true;
        }


        if (
                tender.getBuyer()
                        .getLegalName()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .contains(
                                query
                        )
        ) {
            return true;
        }


        return tender
                .getCpvs()
                .stream()
                .anyMatch(
                        cpv ->
                                cpv.getCode()
                                        .contains(
                                                query
                                        )
                                ||
                                cpv.getLabel()
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                                        .contains(
                                                query
                                        )
                );
    }


    private boolean matchesQuality(
            TenderEntity tender,
            Integer minQualityScore,
            DataQualityGrade grade
    ) {

        if (
                minQualityScore == null
                &&
                grade == null
        ) {
            return true;
        }


        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tender.getId()
                        )
                        .orElse(
                                null
                        );


        if (quality == null) {
            return false;
        }


        if (
                minQualityScore != null
                &&
                quality.getScore()
                        <
                        minQualityScore
        ) {
            return false;
        }


        return grade == null
                ||
                quality.getGrade()
                        ==
                        grade;
    }


    private OpportunitySummaryResponse summary(
            TenderEntity tender
    ) {

        TenderAwardEntity award =
                awardRepository
                        .findByTender_Id(
                                tender.getId()
                        )
                        .orElse(
                                null
                        );


        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tender.getId()
                        )
                        .orElse(
                                null
                        );


        return new OpportunitySummaryResponse(
                tender.getId(),
                tender.getTitle(),
                tender
                        .getBuyer()
                        .getLegalName(),
                tender.getStatus(),
                tender.getProcedureType(),
                tender.getContractNature(),
                tender.getPublicationDate(),
                tender.getSubmissionDeadline(),
                tender.getEstimatedMinValue(),
                tender.getEstimatedMaxValue(),
                tender.getCurrency(),
                tender.getCountryCode(),
                tender.getPrimaryRegionName(),

                tender
                        .getCpvs()
                        .stream()
                        .limit(
                                3
                        )
                        .map(
                                cpv ->
                                        new OpportunitySummaryResponse
                                                .CpvSummary(
                                                        cpv.getCode(),
                                                        cpv.getLabel()
                                                )
                        )
                        .toList(),

                tender
                        .getSourceRecords()
                        .stream()
                        .map(
                                record ->
                                        record.getSource()
                        )
                        .distinct()
                        .toList(),

                tender
                        .getLots()
                        .size(),

                award == null
                        ? null
                        : award.getAwardedAmount(),

                award == null
                        ? null
                        : award.getCurrency(),

                quality == null
                        ? null
                        : quality.getScore(),

                quality == null
                        ? null
                        : quality.getGrade(),

                quality == null
                        ? 0
                        : quality.getIssueCount()
        );
    }


    private OpportunityDetailResponse detailResponse(
            TenderEntity tender
    ) {

        return new OpportunityDetailResponse(
                tender.getId(),
                tender.getCanonicalKey(),
                tender.getTitle(),
                tender.getDescription(),
                tender.getStatus(),
                tender.getProcedureType(),
                tender.getContractNature(),
                tender.getPublicationDate(),
                tender.getSubmissionDeadline(),
                tender.getEstimatedMinValue(),
                tender.getEstimatedMaxValue(),
                tender.getCurrency(),
                tender.getCountryCode(),
                tender.getPrimaryRegionCode(),
                tender.getPrimaryRegionName(),
                tender.isFrameworkAgreement(),
                tender.isJointProcurement(),
                tender.isElectronicSubmission(),

                new OpportunityDetailResponse
                        .BuyerResponse(
                                tender
                                        .getBuyer()
                                        .getId(),
                                tender
                                        .getBuyer()
                                        .getLegalName(),
                                tender
                                        .getBuyer()
                                        .getCategory(),
                                tender
                                        .getBuyer()
                                        .getCountryCode(),
                                tender
                                        .getBuyer()
                                        .getCity(),
                                tender
                                        .getBuyer()
                                        .getRegistrationNumber(),
                                tender
                                        .getBuyer()
                                        .getWebsiteUrl()
                        ),

                tender
                        .getCpvs()
                        .stream()
                        .map(
                                this::cpv
                        )
                        .toList(),

                tender
                        .getLocations()
                        .stream()
                        .map(
                                location ->
                                        new OpportunityDetailResponse
                                                .LocationResponse(
                                                        location.getCountryCode(),
                                                        location.getRegionCode(),
                                                        location.getRegionName(),
                                                        location.getCity()
                                                )
                        )
                        .toList(),

                tender
                        .getLots()
                        .stream()
                        .map(
                                lot ->
                                        new OpportunityDetailResponse
                                                .LotResponse(
                                                        lot.getId(),
                                                        lot.getLotNumber(),
                                                        lot.getTitle(),
                                                        lot.getDescription(),
                                                        lot.getEstimatedMinValue(),
                                                        lot.getEstimatedMaxValue(),
                                                        lot.getCurrency(),
                                                        lot
                                                                .getCpvs()
                                                                .stream()
                                                                .map(
                                                                        this::cpv
                                                                )
                                                                .toList()
                                                )
                        )
                        .toList(),

                tender
                        .getSourceRecords()
                        .stream()
                        .map(
                                source ->
                                        new OpportunityDetailResponse
                                                .SourceResponse(
                                                        source.getSource(),
                                                        source.getExternalId(),
                                                        source.getExternalVersion(),
                                                        source.getNoticeType(),
                                                        source.getSourceUrl(),
                                                        source.getPayloadChecksum(),
                                                        source.getSourcePublishedAt(),
                                                        source.getFirstSeenAt(),
                                                        source.getLastSeenAt()
                                                )
                        )
                        .toList(),

                awardResponse(
                        tender.getId()
                ),

                qualityResponse(
                        tender.getId()
                )
        );
    }


    private OpportunityDetailResponse.AwardResponse
    awardResponse(
            UUID tenderId
    ) {

        TenderAwardEntity award =
                awardRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElse(
                                null
                        );


        if (award == null) {
            return null;
        }


        return new OpportunityDetailResponse
                .AwardResponse(
                        award.getNotificationDate(),
                        award.getAwardedAmount(),
                        award.getCurrency(),
                        award.getDurationMonths(),
                        award.getPriceForm(),
                        award.getOffersReceived(),
                        award.getFrameworkAgreementId(),
                        award.getSocialConsiderations(),
                        award.getEnvironmentalConsiderations(),
                        award.getExecutionModalities(),
                        award.getOperatorGroupType(),
                        award.getSubcontractingDeclared(),

                        award
                                .getSuppliers()
                                .stream()
                                .map(
                                        supplier ->
                                                new OpportunityDetailResponse
                                                        .AwardSupplierResponse(
                                                                supplier.getIdentifier(),
                                                                supplier.getIdentifierType(),
                                                                supplier.getLegalName()
                                                        )
                                )
                                .toList()
                );
    }


    private OpportunityDetailResponse.DataQualityResponse
    qualityResponse(
            UUID tenderId
    ) {

        ProcurementDataQualityAssessmentEntity quality =
                qualityRepository
                        .findByTender_Id(
                                tenderId
                        )
                        .orElse(
                                null
                        );


        if (quality == null) {
            return null;
        }


        return new OpportunityDetailResponse
                .DataQualityResponse(
                        quality.getScore(),
                        quality.getGrade(),
                        quality.getCriticalIssueCount(),
                        quality.getWarningIssueCount(),
                        quality.getInfoIssueCount(),
                        quality.getAssessedAt(),

                        quality
                                .getIssues()
                                .stream()
                                .map(
                                        issue ->
                                                new OpportunityDetailResponse
                                                        .DataQualityIssueResponse(
                                                                issue.getCode(),
                                                                issue.getSeverity(),
                                                                issue.getFieldName(),
                                                                issue.getMessage()
                                                        )
                                )
                                .toList()
                );
    }


    private OpportunityDetailResponse.CpvResponse cpv(
            CpvClassificationValue value
    ) {

        return new OpportunityDetailResponse
                .CpvResponse(
                        value.getCode(),
                        value.getLabel()
                );
    }

}
