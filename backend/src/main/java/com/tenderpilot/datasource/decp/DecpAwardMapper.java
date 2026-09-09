package com.tenderpilot.datasource.decp;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;
import com.tenderpilot.procurement.award.application.CanonicalAwardUpsertRequest;

import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

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
public class DecpAwardMapper {

    private final DecpSettings settings;

    private final DecpExecutionLocationNormalizer
            locationNormalizer;

    private final DecpBuyerClassifier
            buyerClassifier;

    public DecpAwardMapper(
            DecpSettings settings,
            DecpExecutionLocationNormalizer locationNormalizer,
            DecpBuyerClassifier buyerClassifier
    ) {
        this.settings = settings;
        this.locationNormalizer =
                locationNormalizer;
        this.buyerClassifier =
                buyerClassifier;
    }

    public DecpMappedContract map(
            DecpAwardRecord record
    ) {

        if (record == null) {
            throw new InvalidRequestException(
                    "DECP award record is required"
            );
        }

        String externalId =
                require(
                        record.externalId(),
                        "DECP contract identifier is required"
                );

        LocalDate publicationDate =
                record.publicationDate() != null
                        ? record.publicationDate()
                        : record.notificationDate();

        if (publicationDate == null) {
            throw new InvalidRequestException(
                    "DECP publication or notification date is required"
            );
        }

        String title =
                clean(record.object());

        if (title == null) {
            title =
                    "French awarded public contract "
                            +
                            externalId;
        }

        String buyerId =
                clean(record.buyerId());

        String buyerName =
                clean(record.buyerName());

        if (buyerName == null) {

            buyerName =
                    buyerId == null
                            ? "French public buyer"
                            : "Public buyer " + buyerId;
        }

        String buyerCanonicalKey =
                buyerId == null
                        ? "DECP-BUYER:"
                                +
                                sha256(buyerName)
                                        .substring(0, 24)
                        : "DECP-BUYER:"
                                +
                                buyerId;

        DecpExecutionLocationNormalizer.NormalizedLocation
                location =
                locationNormalizer
                        .normalize(
                                record.executionPlaceCode(),
                                record.executionPlaceType(),
                                record.executionPlaceName()
                        );

        List<CanonicalTenderUpsertRequest.LocationInput>
                locations =
                location == null
                        ? List.of()
                        : List.of(
                                new CanonicalTenderUpsertRequest
                                        .LocationInput(
                                                location.countryCode(),
                                                location.regionCode(),
                                                location.regionName(),
                                                location.city()
                                        )
                        );

        String cpv =
                normalizeCpv(
                        record.cpvCode()
                );

        List<CanonicalTenderUpsertRequest.CpvInput>
                cpvs =
                cpv == null
                        ? List.of()
                        : List.of(
                                new CanonicalTenderUpsertRequest
                                        .CpvInput(
                                                cpv,
                                                "CPV " + cpv
                                        )
                        );

        String upstream =
                clean(
                        record.upstreamSource()
                );

        String externalVersion =
                publicationDate
                        +
                        (
                                upstream == null
                                        ? ""
                                        : ":" + upstream
                        );

        CanonicalTenderUpsertRequest tender =
                new CanonicalTenderUpsertRequest(
                        ProcurementSource.DECP,

                        externalId,

                        externalVersion,

                        "AWARDED_CONTRACT",

                        settings.datasetPageUrl(),

                        fingerprint(record),

                        publicationDate
                                .atStartOfDay(
                                        ZoneId.of(
                                                "Europe/Paris"
                                        )
                                )
                                .toInstant(),

                        new CanonicalTenderUpsertRequest
                                .BuyerInput(
                                        buyerCanonicalKey,
                                        buyerName,
                                        buyerClassifier
                                                .classify(
                                                        buyerName
                                                ),
                                        "FR",
                                        location == null
                                                ? null
                                                : location.city(),
                                        buyerId,
                                        null
                                ),

                        title,

                        title,

                        ProcurementStatus.AWARDED,

                        mapProcedure(
                                record.procedure()
                        ),

                        mapContractNatureFromCpv(
                                cpv
                        ),

                        publicationDate,

                        null,

                        /*
                         * DECP montant is an AWARD value.
                         * It must never become an estimated
                         * tender value.
                         */
                        null,

                        null,

                        null,

                        "FR",

                        location == null
                                ? null
                                : location.regionCode(),

                        location == null
                                ? null
                                : location.regionName(),

                        clean(
                                record.frameworkAgreementId()
                        ) != null,

                        false,

                        false,

                        cpvs,

                        locations,

                        List.of()
                );

        CanonicalAwardUpsertRequest award =
                new CanonicalAwardUpsertRequest(
                        record.notificationDate(),

                        record.awardedAmount(),

                        record.awardedAmount() == null
                                ? null
                                : "EUR",

                        record.durationMonths(),

                        clean(record.priceForm()),

                        record.offersReceived(),

                        clean(
                                record.frameworkAgreementId()
                        ),

                        clean(
                                record.socialConsiderations()
                        ),

                        clean(
                                record.environmentalConsiderations()
                        ),

                        clean(
                                record.executionModalities()
                        ),

                        clean(
                                record.operatorGroupType()
                        ),

                        clean(
                                record.subcontractingDeclared()
                        ),

                        safeList(
                                record.suppliers()
                        )
                                .stream()
                                .map(
                                        supplier ->
                                                new CanonicalAwardUpsertRequest
                                                        .SupplierInput(
                                                                clean(
                                                                        supplier.identifier()
                                                                ),
                                                                clean(
                                                                        supplier.identifierType()
                                                                ),
                                                                clean(
                                                                        supplier.legalName()
                                                                )
                                                        )
                                )
                                .toList()
                );

        return new DecpMappedContract(
                tender,
                award
        );
    }

    private ProcurementProcedure mapProcedure(
            String value
    ) {

        String normalized =
                normalized(value);

        if (normalized == null) {
            return ProcurementProcedure.OTHER;
        }

        if (normalized.contains("dialogue")) {
            return ProcurementProcedure.COMPETITIVE_DIALOGUE;
        }

        if (normalized.contains("innovation")) {
            return ProcurementProcedure.INNOVATION_PARTNERSHIP;
        }

        if (normalized.contains("avec negociation")) {
            return ProcurementProcedure.COMPETITIVE_WITH_NEGOTIATION;
        }

        if (normalized.contains("restreint")) {
            return ProcurementProcedure.RESTRICTED;
        }

        if (normalized.contains("ouvert")) {
            return ProcurementProcedure.OPEN;
        }

        if (
                normalized.contains("sans publicite")
                ||
                normalized.contains(
                        "sans mise en concurrence"
                )
        ) {
            return ProcurementProcedure.DIRECT_AWARD;
        }

        if (normalized.contains("negoc")) {
            return ProcurementProcedure.NEGOTIATED;
        }

        return ProcurementProcedure.OTHER;
    }

    private ContractNature mapContractNatureFromCpv(
            String cpv
    ) {

        if (
                cpv == null
                ||
                cpv.length() < 2
        ) {
            return ContractNature.OTHER;
        }

        int division;

        try {
            division =
                    Integer.parseInt(
                            cpv.substring(0, 2)
                    );
        } catch (
                NumberFormatException exception
        ) {
            return ContractNature.OTHER;
        }

        if (division == 45) {
            return ContractNature.WORKS;
        }

        if (
                division >= 50
                &&
                division <= 98
        ) {
            return ContractNature.SERVICES;
        }

        if (
                division >= 3
                &&
                division <= 44
        ) {
            return ContractNature.SUPPLIES;
        }

        if (division == 48) {
            return ContractNature.SUPPLIES;
        }

        return ContractNature.OTHER;
    }

    private String normalizeCpv(
            String value
    ) {

        String cleaned = clean(value);

        if (cleaned == null) {
            return null;
        }

        String digits =
                cleaned.replaceAll(
                        "[^0-9]",
                        ""
                );

        return digits.length() >= 8
                ? digits.substring(0, 8)
                : null;
    }

    private String fingerprint(
            DecpAwardRecord record
    ) {

        return sha256(
                String.join(
                        "|",

                        nullSafe(
                                record.externalId()
                        ),

                        nullSafe(
                                record.object()
                        ),

                        nullSafe(
                                dateText(
                                        record.publicationDate()
                                )
                        ),

                        nullSafe(
                                dateText(
                                        record.notificationDate()
                                )
                        ),

                        nullSafe(
                                record.awardedAmount() == null
                                        ? null
                                        : record
                                                .awardedAmount()
                                                .toPlainString()
                        ),

                        nullSafe(
                                record.upstreamSource()
                        )
                )
        );
    }

    private String dateText(LocalDate value) {
        return value == null
                ? null
                : value.toString();
    }

    private String sha256(String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
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

    private String normalized(String value) {

        String cleaned = clean(value);

        if (cleaned == null) {
            return null;
        }

        return Normalizer
                .normalize(
                        cleaned,
                        Normalizer.Form.NFD
                )
                .replaceAll(
                        "\\p{M}+",
                        ""
                )
                .toLowerCase(Locale.ROOT);
    }

    private String require(
            String value,
            String message
    ) {

        String cleaned = clean(value);

        if (cleaned == null) {
            throw new InvalidRequestException(message);
        }

        return cleaned;
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

    private <T> List<T> safeList(
            List<T> values
    ) {
        return values == null
                ? List.of()
                : values;
    }
}
