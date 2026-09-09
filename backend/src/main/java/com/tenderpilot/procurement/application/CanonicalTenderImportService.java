package com.tenderpilot.procurement.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.procurement.quality.application.ProcurementDataQualityService;
import com.tenderpilot.search.application.SearchProjectionService;

import com.tenderpilot.procurement.domain.CpvClassificationValue;
import com.tenderpilot.procurement.domain.ProcurementBuyerEntity;
import com.tenderpilot.procurement.domain.ProcurementLocationValue;
import com.tenderpilot.procurement.domain.TenderEntity;
import com.tenderpilot.procurement.domain.TenderLotEntity;
import com.tenderpilot.procurement.domain.TenderSourceRecordEntity;

import com.tenderpilot.procurement.infrastructure.ProcurementBuyerRepository;
import com.tenderpilot.procurement.infrastructure.TenderRepository;
import com.tenderpilot.procurement.infrastructure.TenderSourceRecordRepository;

import java.math.BigDecimal;

import java.net.URI;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CanonicalTenderImportService {

    private final ProcurementBuyerRepository
            buyerRepository;


    private final TenderRepository
            tenderRepository;


    private final TenderSourceRecordRepository
            sourceRecordRepository;


    private final ProcurementDataQualityService
            dataQualityService;


    private final SearchProjectionService
            searchProjectionService;


    public CanonicalTenderImportService(

            ProcurementBuyerRepository buyerRepository,

            TenderRepository tenderRepository,

            TenderSourceRecordRepository sourceRecordRepository,

            ProcurementDataQualityService dataQualityService,

            SearchProjectionService searchProjectionService

    ) {

        this.buyerRepository =
                buyerRepository;

        this.tenderRepository =
                tenderRepository;

        this.sourceRecordRepository =
                sourceRecordRepository;

        this.dataQualityService =
                dataQualityService;


        this.searchProjectionService =
                searchProjectionService;

    }


    @Transactional
    public UUID upsert(
            CanonicalTenderUpsertRequest request
    ) {

        validate(
                request
        );


        String externalId =
                cleanRequired(
                        request.externalId()
                );


        ProcurementBuyerEntity buyer =
                resolveBuyer(
                        request.buyer()
                );


        TenderSourceRecordEntity existingSource =

                sourceRecordRepository
                        .findBySourceAndExternalId(

                                request.source(),

                                externalId

                        )
                        .orElse(
                                null
                        );


        TenderEntity tender;


        if (
                existingSource == null
        ) {

            tender =
                    TenderEntity
                            .create(
                                    canonicalKey(

                                            request.source()
                                                    .name(),

                                            externalId

                                    )
                            );

        } else {

            tender =
                    existingSource
                            .getTender();

        }


        tender.update(

                buyer,

                cleanRequired(
                        request.title()
                ),

                cleanOptional(
                        request.description()
                )
                        == null
                        ? ""
                        : cleanOptional(
                                request.description()
                        ),

                request.status(),

                request.procedureType(),

                request.contractNature(),

                request.publicationDate(),

                request.submissionDeadline(),

                request.estimatedMinValue(),

                request.estimatedMaxValue(),

                upperOptional(
                        request.currency()
                ),

                upper(
                        request.countryCode()
                ),

                upperOptional(
                        request.primaryRegionCode()
                ),

                cleanOptional(
                        request.primaryRegionName()
                ),

                request.frameworkAgreement(),

                request.jointProcurement(),

                request.electronicSubmission()

        );


        tender.replaceCpvs(
                normalizeCpvs(
                        request.cpvs()
                )
        );


        tender.replaceLocations(
                normalizeLocations(
                        request.locations()
                )
        );


        tender.replaceLots(
                normalizeLots(

                        tender,

                        request.lots()

                )
        );


        if (
                existingSource == null
        ) {

            TenderSourceRecordEntity sourceRecord =

                    TenderSourceRecordEntity
                            .create(

                                    tender,

                                    request.source(),

                                    externalId

                            );


            sourceRecord.touch(

                    cleanOptional(
                            request.externalVersion()
                    ),

                    cleanOptional(
                            request.noticeType()
                    ),

                    normalizeUrl(
                            request.sourceUrl()
                    ),

                    cleanOptional(
                            request.payloadChecksum()
                    ),

                    request.sourcePublishedAt()

            );


            tender.addSourceRecord(
                    sourceRecord
            );


        } else {

            existingSource.touch(

                    cleanOptional(
                            request.externalVersion()
                    ),

                    cleanOptional(
                            request.noticeType()
                    ),

                    normalizeUrl(
                            request.sourceUrl()
                    ),

                    cleanOptional(
                            request.payloadChecksum()
                    ),

                    request.sourcePublishedAt()

            );

        }


        TenderEntity saved =

                tenderRepository
                        .saveAndFlush(
                                tender
                        );


        dataQualityService
                .assessAndPersist(
                        saved.getId()
                );


        searchProjectionService
                .indexAfterWrite(
                        saved.getId()
                );


        return saved.getId();

    }


    private ProcurementBuyerEntity resolveBuyer(
            CanonicalTenderUpsertRequest.BuyerInput input
    ) {

        String canonicalKey =
                cleanRequired(
                        input.canonicalKey()
                );


        ProcurementBuyerEntity buyer =

                buyerRepository
                        .findByCanonicalKey(
                                canonicalKey
                        )
                        .orElseGet(
                                () ->
                                        ProcurementBuyerEntity
                                                .create(
                                                        canonicalKey
                                                )
                        );


        buyer.update(

                cleanRequired(
                        input.legalName()
                ),

                input.category(),

                upper(
                        input.countryCode()
                ),

                cleanOptional(
                        input.city()
                ),

                cleanOptional(
                        input.registrationNumber()
                ),

                normalizeUrl(
                        input.websiteUrl()
                )

        );


        return buyerRepository
                .save(
                        buyer
                );

    }


    private List<CpvClassificationValue> normalizeCpvs(
            List<CanonicalTenderUpsertRequest.CpvInput> inputs
    ) {

        Map<String, CpvClassificationValue> distinct =
                new LinkedHashMap<>();


        for (
                CanonicalTenderUpsertRequest.CpvInput input :
                safeList(
                        inputs
                )
        ) {

            String code =
                    cleanRequired(
                            input.code()
                    );


            if (
                    !code.matches(
                            "\\d{8}"
                    )
            ) {

                throw new InvalidRequestException(
                        "CPV code must contain exactly 8 digits"
                );

            }


            distinct.putIfAbsent(

                    code,

                    new CpvClassificationValue(

                            code,

                            cleanRequired(
                                    input.label()
                            )

                    )

            );

        }


        return List.copyOf(
                distinct.values()
        );

    }


    private List<ProcurementLocationValue> normalizeLocations(
            List<CanonicalTenderUpsertRequest.LocationInput> inputs
    ) {

        Map<String, ProcurementLocationValue> distinct =
                new LinkedHashMap<>();


        for (
                CanonicalTenderUpsertRequest.LocationInput input :
                safeList(
                        inputs
                )
        ) {

            ProcurementLocationValue location =

                    new ProcurementLocationValue(

                            upper(
                                    input.countryCode()
                            ),

                            upperOptional(
                                    input.regionCode()
                            ),

                            cleanOptional(
                                    input.regionName()
                            ),

                            cleanOptional(
                                    input.city()
                            )

                    );


            String key =

                    location.getCountryCode()
                            +
                            "|"
                            +
                            nullSafe(
                                    location.getRegionCode()
                            )
                            +
                            "|"
                            +
                            nullSafe(
                                    location.getCity()
                            );


            distinct.putIfAbsent(
                    key,
                    location
            );

        }


        return List.copyOf(
                distinct.values()
        );

    }


    private List<TenderLotEntity> normalizeLots(

            TenderEntity tender,

            List<CanonicalTenderUpsertRequest.LotInput> inputs

    ) {

        return safeList(
                inputs
        )
                .stream()
                .map(
                        input -> {

                            validateRange(

                                    input.estimatedMinValue(),

                                    input.estimatedMaxValue(),

                                    "Lot minimum estimated value cannot exceed its maximum estimated value"

                            );


                            requireCurrencyWhenValued(

                                    input.estimatedMinValue(),

                                    input.estimatedMaxValue(),

                                    input.currency(),

                                    "Lot currency is required when an estimated value is supplied"

                            );


                            return TenderLotEntity
                                    .create(

                                            tender,

                                            cleanRequired(
                                                    input.lotNumber()
                                            ),

                                            cleanRequired(
                                                    input.title()
                                            ),

                                            cleanOptional(
                                                    input.description()
                                            )
                                                    == null
                                                    ? ""
                                                    : cleanOptional(
                                                            input.description()
                                                    ),

                                            input.estimatedMinValue(),

                                            input.estimatedMaxValue(),

                                            upperOptional(
                                                    input.currency()
                                            ),

                                            normalizeCpvs(
                                                    input.cpvs()
                                            )

                                    );

                        }
                )
                .toList();

    }


    private void validate(
            CanonicalTenderUpsertRequest request
    ) {

        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Canonical tender request is required"
            );

        }


        if (
                request.source() == null
        ) {

            throw new InvalidRequestException(
                    "Procurement source is required"
            );

        }


        requirePresent(

                request.externalId(),

                "External tender identifier is required"

        );


        if (
                request.buyer() == null
        ) {

            throw new InvalidRequestException(
                    "Buyer information is required"
            );

        }


        requirePresent(

                request.buyer()
                        .canonicalKey(),

                "Buyer canonical key is required"

        );


        requirePresent(

                request.buyer()
                        .legalName(),

                "Buyer legal name is required"

        );


        if (
                request.buyer()
                        .category()
                        ==
                        null
        ) {

            throw new InvalidRequestException(
                    "Buyer category is required"
            );

        }


        requireCountry(
                request.buyer()
                        .countryCode()
        );


        requirePresent(

                request.title(),

                "Tender title is required"

        );


        if (
                request.status() == null
        ) {

            throw new InvalidRequestException(
                    "Tender status is required"
            );

        }


        if (
                request.procedureType() == null
        ) {

            throw new InvalidRequestException(
                    "Tender procedure type is required"
            );

        }


        if (
                request.contractNature() == null
        ) {

            throw new InvalidRequestException(
                    "Tender contract nature is required"
            );

        }


        if (
                request.publicationDate() == null
        ) {

            throw new InvalidRequestException(
                    "Tender publication date is required"
            );

        }


        requireCountry(
                request.countryCode()
        );


        validateRange(

                request.estimatedMinValue(),

                request.estimatedMaxValue(),

                "Tender minimum estimated value cannot exceed its maximum estimated value"

        );


        requireCurrencyWhenValued(

                request.estimatedMinValue(),

                request.estimatedMaxValue(),

                request.currency(),

                "Tender currency is required when an estimated value is supplied"

        );

    }


    private void validateRange(

            BigDecimal minimum,

            BigDecimal maximum,

            String message

    ) {

        if (
                minimum != null
                &&
                maximum != null
                &&
                minimum.compareTo(
                        maximum
                )
                >
                0
        ) {

            throw new InvalidRequestException(
                    message
            );

        }

    }


    private void requireCurrencyWhenValued(

            BigDecimal minimum,

            BigDecimal maximum,

            String currency,

            String message

    ) {

        if (
                (
                        minimum != null
                        ||
                        maximum != null
                )
                &&
                cleanOptional(
                        currency
                )
                ==
                null
        ) {

            throw new InvalidRequestException(
                    message
            );

        }

    }


    private void requireCountry(
            String countryCode
    ) {

        String normalized =
                upper(
                        countryCode
                );


        if (
                !normalized.matches(
                        "[A-Z]{2}"
                )
        ) {

            throw new InvalidRequestException(
                    "Country code must contain exactly two letters"
            );

        }

    }


    private void requirePresent(

            String value,

            String message

    ) {

        if (
                cleanOptional(
                        value
                )
                ==
                null
        ) {

            throw new InvalidRequestException(
                    message
            );

        }

    }


    private String normalizeUrl(
            String raw
    ) {

        String value =
                cleanOptional(
                        raw
                );


        if (
                value == null
        ) {

            return null;

        }


        try {

            URI uri =
                    URI.create(
                            value
                    );


            if (
                    !(
                            "http".equalsIgnoreCase(
                                    uri.getScheme()
                            )
                            ||
                            "https".equalsIgnoreCase(
                                    uri.getScheme()
                            )
                    )
                    ||
                    uri.getHost()
                            ==
                            null
            ) {

                throw new InvalidRequestException(
                        "URL must be a valid http or https URL"
                );

            }


            return uri.toString();


        } catch (
                IllegalArgumentException exception
        ) {

            throw new InvalidRequestException(
                    "URL is invalid"
            );

        }

    }


    private String canonicalKey(

            String source,

            String externalId

    ) {

        return source
                +
                ":"
                +
                externalId;

    }


    private String cleanRequired(
            String value
    ) {

        if (
                value == null
        ) {

            throw new InvalidRequestException(
                    "Required text value is missing"
            );

        }


        String cleaned =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        if (
                cleaned.isBlank()
        ) {

            throw new InvalidRequestException(
                    "Required text value is blank"
            );

        }


        return cleaned;

    }


    private String cleanOptional(
            String value
    ) {

        if (
                value == null
        ) {

            return null;

        }


        String cleaned =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return cleaned.isBlank()
                ? null
                : cleaned;

    }


    private String upper(
            String value
    ) {

        return cleanRequired(
                value
        )
                .toUpperCase(
                        Locale.ROOT
                );

    }


    private String upperOptional(
            String value
    ) {

        String cleaned =
                cleanOptional(
                        value
                );


        return cleaned == null
                ? null
                : cleaned.toUpperCase(
                        Locale.ROOT
                );

    }


    private String nullSafe(
            String value
    ) {

        return value == null
                ? ""
                : value;

    }


    private <T> List<T> safeList(
            List<T> values
    ) {

        return values == null
                ? List.of()
                : values;

    }

}
