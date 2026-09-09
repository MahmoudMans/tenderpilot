package com.tenderpilot.datasource.ted;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import java.math.BigDecimal;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class TedNoticeMapper {

    private final TedCountryCodeNormalizer
            countryCodeNormalizer;


    public TedNoticeMapper(
            TedCountryCodeNormalizer countryCodeNormalizer
    ) {

        this.countryCodeNormalizer =
                countryCodeNormalizer;

    }


    public CanonicalTenderUpsertRequest map(
            TedSearchNotice notice
    ) {

        if (
                notice == null
        ) {

            throw new InvalidRequestException(
                    "TED notice is required"
            );

        }


        String externalId =
                require(
                        notice.externalId(),
                        "TED publication number is required"
                );


        LocalDate publicationDate =
                notice.publicationDate();


        if (
                publicationDate == null
        ) {

            throw new InvalidRequestException(
                    "TED publication date is required"
            );

        }


        TedBuyerData buyer =
                notice.buyer();


        String buyerName =

                buyer == null
                        ? null
                        : clean(
                                buyer.name()
                        );


        if (
                buyerName == null
        ) {

            buyerName =
                    "TED contracting authority";

        }


        String buyerCountry =

                buyer == null
                        ? null
                        : countryCodeNormalizer
                                .normalize(
                                        buyer.countryCode()
                                );


        List<CanonicalTenderUpsertRequest.LocationInput>
                locations =

                mapLocations(
                        notice.locations()
                );


        String tenderCountry =

                locations.isEmpty()

                        ? buyerCountry

                        : locations
                                .getFirst()
                                .countryCode();


        if (
                tenderCountry == null
        ) {

            tenderCountry =
                    "EU";

        }


        String primaryRegionCode =

                locations.isEmpty()
                        ? null
                        : locations
                                .getFirst()
                                .regionCode();


        String primaryRegionName =
                primaryRegionCode;


        BigDecimal estimatedValue =
                notice.estimatedValue();


        String currency =
                normalizeCurrency(
                        notice.currency()
                );


        if (
                estimatedValue != null
                &&
                currency == null
        ) {

            // Keep the canonical record truthful rather than inventing currency.
            estimatedValue =
                    null;

        }


        String title =
                clean(
                        notice.title()
                );


        if (
                title == null
        ) {

            title =
                    "TED notice "
                            +
                            externalId;

        }


        String description =
                clean(
                        notice.description()
                );


        if (
                description == null
        ) {

            description =
                    title;

        }


        CanonicalTenderUpsertRequest.BuyerInput
                buyerInput =

                new CanonicalTenderUpsertRequest
                        .BuyerInput(

                                buyerCanonicalKey(

                                        buyerCountry,

                                        buyer == null
                                                ? null
                                                : buyer.identifier(),

                                        buyerName

                                ),

                                buyerName,

                                mapBuyerCategory(
                                        buyer == null
                                                ? null
                                                : buyer.legalType()
                                ),

                                buyerCountry == null
                                        ? tenderCountry
                                        : buyerCountry,

                                buyer == null
                                        ? null
                                        : clean(
                                                buyer.city()
                                        ),

                                buyer == null
                                        ? null
                                        : clean(
                                                buyer.identifier()
                                        ),

                                buyer == null
                                        ? null
                                        : urlOrNull(
                                                buyer.websiteUrl()
                                        )

                        );


        List<CanonicalTenderUpsertRequest.CpvInput>
                cpvs =

                safeList(
                        notice.cpvCodes()
                )
                        .stream()
                        .filter(
                                value ->
                                        value != null
                                        &&
                                        value.matches(
                                                "\\d{8}"
                                        )
                        )
                        .distinct()
                        .map(
                                value ->
                                        new CanonicalTenderUpsertRequest
                                                .CpvInput(

                                                        value,

                                                        "CPV "
                                                                +
                                                                value

                                                )
                        )
                        .toList();


        Instant deadline =
                submissionDeadline(
                        notice
                );


        return new CanonicalTenderUpsertRequest(

                ProcurementSource.TED,

                externalId,

                clean(
                        notice.externalVersion()
                ),

                clean(
                        notice.noticeType()
                ),

                urlOrNull(
                        notice.sourceUrl()
                ),

                fingerprint(
                        notice
                ),

                publicationDate
                        .atStartOfDay(
                                ZoneOffset.UTC
                        )
                        .toInstant(),

                buyerInput,

                title,

                description,

                ProcurementStatus.PUBLISHED,

                mapProcedure(
                        notice.procedureType()
                ),

                mapNature(
                        notice.contractNature()
                ),

                publicationDate,

                deadline,

                estimatedValue,

                estimatedValue,

                currency,

                tenderCountry,

                primaryRegionCode,

                primaryRegionName,

                notice.frameworkAgreement(),

                false,

                notice.electronicSubmission(),

                cpvs,

                locations,

                List.of()

        );

    }


    private List<CanonicalTenderUpsertRequest.LocationInput>
    mapLocations(
            List<TedLocationData> values
    ) {

        List<CanonicalTenderUpsertRequest.LocationInput>
                result =
                new ArrayList<>();


        for (
                TedLocationData value :
                safeList(
                        values
                )
        ) {

            String country =
                    countryCodeNormalizer
                            .normalize(
                                    value.countryCode()
                            );


            if (
                    country == null
            ) {

                continue;

            }


            result.add(

                    new CanonicalTenderUpsertRequest
                            .LocationInput(

                                    country,

                                    clean(
                                            value.regionCode()
                                    ),

                                    clean(
                                            value.regionCode()
                                    ),

                                    clean(
                                            value.city()
                                    )

                            )

            );

        }


        return List.copyOf(
                result
        );

    }


    private Instant submissionDeadline(
            TedSearchNotice notice
    ) {

        List<LocalDate> dates =
                safeList(
                        notice.deadlineDates()
                );


        if (
                dates.isEmpty()
        ) {

            return null;

        }


        LocalDate earliest =

                dates.stream()
                        .min(
                                LocalDate::compareTo
                        )
                        .orElse(
                                null
                        );


        if (
                earliest == null
        ) {

            return null;

        }


        LocalTime time =

                notice.deadlineTime()
                        == null

                        ? LocalTime.of(
                                23,
                                59
                        )

                        : notice.deadlineTime();


        return earliest
                .atTime(
                        time
                )
                .toInstant(
                        ZoneOffset.UTC
                );

    }


    private ProcurementProcedure mapProcedure(
            String raw
    ) {

        String value =
                lower(
                        raw
                );


        if (
                value == null
        ) {

            return ProcurementProcedure.OTHER;

        }


        if (
                value.contains(
                        "open"
                )
        ) {

            return ProcurementProcedure.OPEN;

        }


        if (
                value.contains(
                        "restrict"
                )
        ) {

            return ProcurementProcedure.RESTRICTED;

        }


        if (
                value.contains(
                        "dialog"
                )
        ) {

            return ProcurementProcedure.COMPETITIVE_DIALOGUE;

        }


        if (
                value.contains(
                        "innovation"
                )
        ) {

            return ProcurementProcedure.INNOVATION_PARTNERSHIP;

        }


        if (
                value.contains(
                        "design"
                )
        ) {

            return ProcurementProcedure.DESIGN_CONTEST;

        }


        if (
                value.contains(
                        "negotiat"
                )
                &&
                (
                        value.contains(
                                "competition"
                        )
                        ||
                        value.contains(
                                "call"
                        )
                )
        ) {

            return ProcurementProcedure.COMPETITIVE_WITH_NEGOTIATION;

        }


        if (
                value.contains(
                        "negotiat"
                )
        ) {

            return ProcurementProcedure.NEGOTIATED;

        }


        if (
                value.contains(
                        "direct"
                )
                ||
                value.contains(
                        "without prior"
                )
        ) {

            return ProcurementProcedure.DIRECT_AWARD;

        }


        return ProcurementProcedure.OTHER;

    }


    private ContractNature mapNature(
            String raw
    ) {

        String value =
                lower(
                        raw
                );


        if (
                value == null
        ) {

            return ContractNature.OTHER;

        }


        if (
                value.contains(
                        "work"
                )
        ) {

            return ContractNature.WORKS;

        }


        if (
                value.contains(
                        "service"
                )
        ) {

            return ContractNature.SERVICES;

        }


        if (
                value.contains(
                        "suppl"
                )
                ||
                value.contains(
                        "goods"
                )
        ) {

            return ContractNature.SUPPLIES;

        }


        if (
                value.contains(
                        "mixed"
                )
        ) {

            return ContractNature.MIXED;

        }


        return ContractNature.OTHER;

    }


    private BuyerCategory mapBuyerCategory(
            String raw
    ) {

        String value =
                lower(
                        raw
                );


        if (
                value == null
        ) {

            return BuyerCategory.OTHER;

        }


        if (
                value.contains(
                        "local"
                )
                ||
                value.contains(
                        "regional"
                )
                ||
                value.contains(
                        "rauth"
                )
        ) {

            return BuyerCategory.LOCAL_AUTHORITY;

        }


        if (
                value.contains(
                        "central"
                )
                ||
                value.contains(
                        "cga"
                )
        ) {

            return BuyerCategory.CENTRAL_GOVERNMENT;

        }


        if (
                value.contains(
                        "education"
                )
                ||
                value.contains(
                        "school"
                )
                ||
                value.contains(
                        "university"
                )
        ) {

            return BuyerCategory.EDUCATION;

        }


        if (
                value.contains(
                        "health"
                )
                ||
                value.contains(
                        "hospital"
                )
        ) {

            return BuyerCategory.HEALTHCARE;

        }


        if (
                value.contains(
                        "housing"
                )
        ) {

            return BuyerCategory.SOCIAL_HOUSING;

        }


        if (
                value.contains(
                        "utilit"
                )
        ) {

            return BuyerCategory.UTILITIES;

        }


        if (
                value.contains(
                        "transport"
                )
        ) {

            return BuyerCategory.TRANSPORT;

        }


        if (
                value.contains(
                        "public"
                )
                ||
                value.contains(
                        "body"
                )
                ||
                value.contains(
                        "agency"
                )
        ) {

            return BuyerCategory.PUBLIC_AGENCY;

        }


        return BuyerCategory.OTHER;

    }


    private String buyerCanonicalKey(

            String country,

            String identifier,

            String buyerName

    ) {

        String raw =

                clean(
                        identifier
                );


        if (
                raw == null
        ) {

            raw =
                    buyerName;

        }


        return "TED-BUYER:"
                +
                (
                        country == null
                                ? "EU"
                                : country
                )
                +
                ":"
                +
                sha256(
                        raw
                )
                        .substring(
                                0,
                                24
                        );

    }


    private String fingerprint(
            TedSearchNotice notice
    ) {

        String source =

                String.join(

                        "|",

                        nullSafe(
                                notice.externalId()
                        ),

                        nullSafe(
                                notice.externalVersion()
                        ),

                        nullSafe(
                                notice.noticeType()
                        ),

                        nullSafe(
                                notice.title()
                        ),

                        nullSafe(
                                notice.publicationDate()
                                        == null
                                        ? null
                                        : notice
                                                .publicationDate()
                                                .toString()
                        )

                );


        return sha256(
                source
        );

    }


    private String sha256(
            String value
    ) {

        try {

            MessageDigest digest =

                    MessageDigest
                            .getInstance(
                                    "SHA-256"
                            );


            return HexFormat
                    .of()
                    .formatHex(

                            digest.digest(

                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )

                            )

                    );


        } catch (
                NoSuchAlgorithmException exception
        ) {

            throw new IllegalStateException(

                    "SHA-256 is unavailable",

                    exception

            );

        }

    }


    private String normalizeCurrency(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );


        if (
                cleaned == null
        ) {

            return null;

        }


        String upper =
                cleaned.toUpperCase(
                        Locale.ROOT
                );


        return upper.matches(
                "[A-Z]{3}"
        )
                ? upper
                : null;

    }


    private String urlOrNull(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );


        if (
                cleaned == null
        ) {

            return null;

        }


        return cleaned.startsWith(
                "https://"
        )
                ||
                cleaned.startsWith(
                        "http://"
                )

                ? cleaned

                : null;

    }


    private String require(

            String value,

            String message

    ) {

        String cleaned =
                clean(
                        value
                );


        if (
                cleaned == null
        ) {

            throw new InvalidRequestException(
                    message
            );

        }


        return cleaned;

    }


    private String lower(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );


        return cleaned == null
                ? null
                : cleaned.toLowerCase(
                        Locale.ROOT
                );

    }


    private String clean(
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
