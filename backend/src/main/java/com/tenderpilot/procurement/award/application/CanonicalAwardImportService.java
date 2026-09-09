package com.tenderpilot.procurement.award.application;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.procurement.award.domain.AwardSupplierValue;
import com.tenderpilot.procurement.award.domain.TenderAwardEntity;
import com.tenderpilot.procurement.award.infrastructure.TenderAwardRepository;
import com.tenderpilot.procurement.domain.TenderEntity;
import com.tenderpilot.procurement.infrastructure.TenderRepository;

import com.tenderpilot.procurement.quality.application.ProcurementDataQualityService;
import com.tenderpilot.search.application.SearchProjectionService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CanonicalAwardImportService {

    private final TenderRepository tenderRepository;
    private final TenderAwardRepository awardRepository;


    private final ProcurementDataQualityService
            dataQualityService;


    private final SearchProjectionService
            searchProjectionService;

    public CanonicalAwardImportService(
            TenderRepository tenderRepository,
            TenderAwardRepository awardRepository,
            ProcurementDataQualityService dataQualityService,
            SearchProjectionService searchProjectionService
    ) {
        this.tenderRepository = tenderRepository;
        this.awardRepository = awardRepository;
        this.dataQualityService = dataQualityService;

        this.searchProjectionService = searchProjectionService;
    }

    @Transactional
    public UUID upsert(
            UUID tenderId,
            CanonicalAwardUpsertRequest request
    ) {

        if (tenderId == null) {
            throw new InvalidRequestException(
                    "Tender identifier is required for an award"
            );
        }

        if (request == null) {
            throw new InvalidRequestException(
                    "Award request is required"
            );
        }

        validateAmount(request.awardedAmount());
        validateNonNegative(
                request.durationMonths(),
                "Award duration cannot be negative"
        );
        validateNonNegative(
                request.offersReceived(),
                "Offers received cannot be negative"
        );

        String currency =
                normalizeCurrency(request.currency());

        if (
                request.awardedAmount() != null
                &&
                currency == null
        ) {
            throw new InvalidRequestException(
                    "Award currency is required when an awarded amount is supplied"
            );
        }

        TenderEntity tender =
                tenderRepository
                        .findById(tenderId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Tender was not found for award import"
                                        )
                        );

        TenderAwardEntity award =
                awardRepository
                        .findByTender_Id(tenderId)
                        .orElseGet(
                                () ->
                                        TenderAwardEntity.create(tender)
                        );

        award.update(
                request.notificationDate(),
                request.awardedAmount(),
                currency,
                request.durationMonths(),
                clean(request.priceForm()),
                request.offersReceived(),
                clean(request.frameworkAgreementId()),
                clean(request.socialConsiderations()),
                clean(request.environmentalConsiderations()),
                clean(request.executionModalities()),
                clean(request.operatorGroupType()),
                clean(request.subcontractingDeclared())
        );

        award.replaceSuppliers(
                normalizeSuppliers(request.suppliers())
        );

        TenderAwardEntity saved =
                awardRepository
                        .saveAndFlush(
                                award
                        );


        dataQualityService
                .assessAndPersist(
                        tenderId
                );


        searchProjectionService
                .indexAfterWrite(
                        tenderId
                );


        return saved.getId();
    }

    private List<AwardSupplierValue> normalizeSuppliers(
            List<CanonicalAwardUpsertRequest.SupplierInput> inputs
    ) {

        Map<String, AwardSupplierValue> unique =
                new LinkedHashMap<>();

        for (
                CanonicalAwardUpsertRequest.SupplierInput input :
                safeList(inputs)
        ) {

            String identifier =
                    clean(input.identifier());

            String identifierType =
                    clean(input.identifierType());

            String legalName =
                    clean(input.legalName());

            if (
                    identifier == null
                    &&
                    legalName == null
            ) {
                continue;
            }

            String key =
                    nullSafe(identifier)
                            +
                            "|"
                            +
                            nullSafe(identifierType)
                            +
                            "|"
                            +
                            nullSafe(legalName);

            unique.putIfAbsent(
                    key,
                    new AwardSupplierValue(
                            identifier,
                            identifierType,
                            legalName
                    )
            );
        }

        return List.copyOf(unique.values());
    }

    private void validateAmount(BigDecimal value) {

        if (
                value != null
                &&
                value.signum() < 0
        ) {
            throw new InvalidRequestException(
                    "Awarded amount cannot be negative"
            );
        }
    }

    private void validateNonNegative(
            Integer value,
            String message
    ) {

        if (
                value != null
                &&
                value < 0
        ) {
            throw new InvalidRequestException(message);
        }
    }

    private String normalizeCurrency(String value) {

        String cleaned = clean(value);

        if (cleaned == null) {
            return null;
        }

        String upper =
                cleaned.toUpperCase(Locale.ROOT);

        if (!upper.matches("[A-Z]{3}")) {
            throw new InvalidRequestException(
                    "Award currency must contain exactly three letters"
            );
        }

        return upper;
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value
                        .trim()
                        .replaceAll("\\s+", " ");

        return cleaned.isBlank()
                ? null
                : cleaned;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null
                ? List.of()
                : values;
    }
}
