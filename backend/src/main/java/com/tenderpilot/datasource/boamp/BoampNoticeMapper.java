package com.tenderpilot.datasource.boamp;

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

import java.text.Normalizer;

import java.time.LocalDate;
import java.time.ZoneId;

import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class BoampNoticeMapper {

    private final BoampDepartmentRegionNormalizer
            geographyNormalizer;


    public BoampNoticeMapper(
            BoampDepartmentRegionNormalizer geographyNormalizer
    ) {

        this.geographyNormalizer =
                geographyNormalizer;

    }


    public CanonicalTenderUpsertRequest map(
            BoampNotice notice
    ) {

        if (
                notice == null
        ) {

            throw new InvalidRequestException(
                    "BOAMP notice is required"
            );

        }


        String externalId =
                require(

                        notice.externalId(),

                        "BOAMP announcement identifier is required"

                );


        LocalDate publicationDate =
                notice.publicationDate();


        if (
                publicationDate == null
        ) {

            throw new InvalidRequestException(
                    "BOAMP publication date is required"
            );

        }


        String title =
                clean(
                        notice.object()
                );


        if (
                title == null
        ) {

            title =
                    "BOAMP notice "
                            +
                            externalId;

        }


        String buyerName =
                clean(
                        notice.buyerName()
                );


        if (
                buyerName == null
        ) {

            buyerName =
                    "Acheteur public BOAMP";

        }


        BoampDepartmentRegionNormalizer.Region
                region =

                geographyNormalizer
                        .normalize(
                                notice.departmentCode()
                        );


        List<CanonicalTenderUpsertRequest.LocationInput>
                locations =

                region == null

                        ? List.of()

                        : List.of(

                                new CanonicalTenderUpsertRequest
                                        .LocationInput(

                                                "FR",

                                                region.regionCode(),

                                                region.regionName(),

                                                null

                                        )

                        );


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

            currency =
                    "EUR";

        }


        String finalBuyerName =
                buyerName;


        CanonicalTenderUpsertRequest.BuyerInput
                buyerInput =

                new CanonicalTenderUpsertRequest
                        .BuyerInput(

                                buyerCanonicalKey(
                                        finalBuyerName
                                ),

                                finalBuyerName,

                                mapBuyerCategory(
                                        finalBuyerName
                                ),

                                "FR",

                                null,

                                null,

                                null

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


        return new CanonicalTenderUpsertRequest(

                ProcurementSource.BOAMP,

                externalId,

                clean(
                        notice.sourceSchema()
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
                                ZoneId.of(
                                        "Europe/Paris"
                                )
                        )
                        .toInstant(),

                buyerInput,

                title,

                title,

                mapStatus(
                        notice.noticeType()
                ),

                mapProcedure(
                        notice.procedure()
                ),

                mapNature(
                        notice.nature()
                ),

                publicationDate,

                notice.responseDeadline(),

                estimatedValue,

                estimatedValue,

                currency,

                "FR",

                region == null
                        ? null
                        : region.regionCode(),

                region == null
                        ? null
                        : region.regionName(),

                false,

                false,

                true,

                cpvs,

                locations,

                List.of()

        );

    }


    private ProcurementStatus mapStatus(
            String raw
    ) {

        String value =
                normalizedText(
                        raw
                );


        if (
                value == null
        ) {

            return ProcurementStatus.PUBLISHED;

        }


        if (
                value.contains(
                        "attribution"
                )
                ||
                value.contains(
                        "resultat"
                )
                ||
                value.contains(
                        "award"
                )
        ) {

            return ProcurementStatus.AWARDED;

        }


        if (
                value.contains(
                        "rectific"
                )
                ||
                value.contains(
                        "modification"
                )
        ) {

            return ProcurementStatus.UPDATED;

        }


        if (
                value.contains(
                        "annulation"
                )
                ||
                value.contains(
                        "cancel"
                )
        ) {

            return ProcurementStatus.CANCELLED;

        }


        return ProcurementStatus.PUBLISHED;

    }


    private ProcurementProcedure mapProcedure(
            String raw
    ) {

        String value =
                normalizedText(
                        raw
                );


        if (
                value == null
        ) {

            return ProcurementProcedure.OTHER;

        }


        if (
                value.contains(
                        "partenariat d innovation"
                )
        ) {

            return ProcurementProcedure.INNOVATION_PARTNERSHIP;

        }


        if (
                value.contains(
                        "dialogue"
                )
        ) {

            return ProcurementProcedure.COMPETITIVE_DIALOGUE;

        }


        if (
                value.contains(
                        "avec negociation"
                )
                ||
                value.contains(
                        "appel d offres avec negociation"
                )
        ) {

            return ProcurementProcedure.COMPETITIVE_WITH_NEGOTIATION;

        }


        if (
                value.contains(
                        "restreint"
                )
        ) {

            return ProcurementProcedure.RESTRICTED;

        }


        if (
                value.contains(
                        "ouvert"
                )
                ||
                value.equals(
                        "open"
                )
        ) {

            return ProcurementProcedure.OPEN;

        }


        if (
                value.contains(
                        "concours"
                )
        ) {

            return ProcurementProcedure.DESIGN_CONTEST;

        }


        if (
                value.contains(
                        "sans publicite"
                )
                ||
                value.contains(
                        "sans mise en concurrence"
                )
                ||
                value.contains(
                        "direct"
                )
        ) {

            return ProcurementProcedure.DIRECT_AWARD;

        }


        if (
                value.contains(
                        "negoc"
                )
        ) {

            return ProcurementProcedure.NEGOTIATED;

        }


        return ProcurementProcedure.OTHER;

    }


    private ContractNature mapNature(
            String raw
    ) {

        String value =
                normalizedText(
                        raw
                );


        if (
                value == null
        ) {

            return ContractNature.OTHER;

        }


        if (
                value.contains(
                        "travaux"
                )
                ||
                value.contains(
                        "works"
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
                        "fourniture"
                )
                ||
                value.contains(
                        "suppl"
                )
        ) {

            return ContractNature.SUPPLIES;

        }


        if (
                value.contains(
                        "mixte"
                )
                ||
                value.contains(
                        "mixed"
                )
        ) {

            return ContractNature.MIXED;

        }


        return ContractNature.OTHER;

    }


    private BuyerCategory mapBuyerCategory(
            String buyerName
    ) {

        String value =
                normalizedText(
                        buyerName
                );


        if (
                value == null
        ) {

            return BuyerCategory.OTHER;

        }


        if (
                value.contains(
                        "universite"
                )
                ||
                value.contains(
                        "ecole"
                )
                ||
                value.contains(
                        "academie"
                )
        ) {

            return BuyerCategory.EDUCATION;

        }


        if (
                value.contains(
                        "hopital"
                )
                ||
                value.contains(
                        "chu"
                )
                ||
                value.contains(
                        "centre hospitalier"
                )
                ||
                value.contains(
                        "sante"
                )
        ) {

            return BuyerCategory.HEALTHCARE;

        }


        if (
                value.contains(
                        "office public de l habitat"
                )
                ||
                value.contains(
                        "oph "
                )
                ||
                value.endsWith(
                        " oph"
                )
        ) {

            return BuyerCategory.SOCIAL_HOUSING;

        }


        if (
                value.contains(
                        "mairie"
                )
                ||
                value.contains(
                        "commune"
                )
                ||
                value.startsWith(
                        "ville "
                )
                ||
                value.equals(
                        "ville"
                )
                ||
                value.contains(
                        "departement"
                )
                ||
                value.contains(
                        "region "
                )
                ||
                value.startsWith(
                        "region "
                )
                ||
                value.contains(
                        "metropole"
                )
                ||
                value.contains(
                        "communaute"
                )
        ) {

            return BuyerCategory.LOCAL_AUTHORITY;

        }


        if (
                value.contains(
                        "ministere"
                )
                ||
                value.contains(
                        "etat "
                )
        ) {

            return BuyerCategory.CENTRAL_GOVERNMENT;

        }


        if (
                value.contains(
                        "transport"
                )
                ||
                value.contains(
                        "sncf"
                )
        ) {

            return BuyerCategory.TRANSPORT;

        }


        return BuyerCategory.PUBLIC_AGENCY;

    }


    private String buyerCanonicalKey(
            String buyerName
    ) {

        return "BOAMP-BUYER:"
                +
                sha256(
                        normalizeIdentifier(
                                buyerName
                        )
                )
                        .substring(
                                0,
                                24
                        );

    }


    private String fingerprint(
            BoampNotice notice
    ) {

        return sha256(

                String.join(

                        "|",

                        nullSafe(
                                notice.externalId()
                        ),

                        nullSafe(
                                notice.internalId()
                        ),

                        nullSafe(
                                notice.sourceSchema()
                        ),

                        nullSafe(
                                notice.object()
                        ),

                        nullSafe(
                                notice.publicationDate()
                                        ==
                                        null

                                        ? null

                                        : notice
                                                .publicationDate()
                                                .toString()
                        ),

                        nullSafe(
                                notice.sourceUrl()
                        )

                )

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


    private String normalizeIdentifier(
            String value
    ) {

        String normalized =
                normalizedText(
                        value
                );


        return normalized == null
                ? "unknown"
                : normalized;

    }


    private String normalizedText(
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


        String decomposed =

                Normalizer.normalize(

                        cleaned,

                        Normalizer.Form.NFD

                );


        return decomposed
                .replaceAll(
                        "\\p{M}+",
                        ""
                )
                .toLowerCase(
                        Locale.ROOT
                );

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
