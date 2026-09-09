package com.tenderpilot.datasource.decp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DecpSearchResponseParser {

    public DecpSearchPage parse(
            Object root
    ) {

        List<?> rawRecords =
                asList(
                        direct(
                                root,
                                "results",
                                "records"
                        )
                );

        List<DecpAwardRecord> records =
                rawRecords
                        .stream()
                        .map(this::unwrap)
                        .map(this::parseRecord)
                        .filter(
                                record ->
                                        record.externalId()
                                                != null
                        )
                        .toList();

        long total =
                longValue(
                        direct(
                                root,
                                "total_count",
                                "totalCount",
                                "nhits"
                        ),
                        records.size()
                );

        return new DecpSearchPage(
                total,
                records
        );
    }

    private Object unwrap(Object raw) {

        if (!(raw instanceof Map<?, ?> map)) {
            return raw;
        }

        Object record =
                mapValue(map, "record");

        if (record instanceof Map<?, ?> recordMap) {

            Object fields =
                    mapValue(
                            recordMap,
                            "fields"
                    );

            if (fields instanceof Map<?, ?>) {
                return fields;
            }
        }

        Object fields =
                mapValue(map, "fields");

        return fields instanceof Map<?, ?>
                ? fields
                : raw;
    }

    private DecpAwardRecord parseRecord(
            Object record
    ) {

        return new DecpAwardRecord(
                text(
                        recursive(record, "id")
                ),

                text(
                        recursive(record, "nature")
                ),

                text(
                        recursive(record, "objet")
                ),

                normalizeCpv(
                        text(
                                recursive(
                                        record,
                                        "codecpv",
                                        "codeCPV"
                                )
                        )
                ),

                text(
                        recursive(
                                record,
                                "procedure"
                        )
                ),

                text(
                        recursive(
                                record,
                                "acheteur_id",
                                "acheteurid"
                        )
                ),

                text(
                        recursive(
                                record,
                                "acheteur_nom",
                                "acheteurnom"
                        )
                ),

                integer(
                        recursive(
                                record,
                                "dureemois",
                                "dureeMois"
                        )
                ),

                date(
                        text(
                                recursive(
                                        record,
                                        "datenotification",
                                        "dateNotification"
                                )
                        )
                ),

                date(
                        text(
                                recursive(
                                        record,
                                        "datepublicationdonnees",
                                        "datePublicationDonnees"
                                )
                        )
                ),

                decimal(
                        recursive(
                                record,
                                "montant"
                        )
                ),

                text(
                        recursive(
                                record,
                                "formeprix",
                                "formePrix"
                        )
                ),

                integer(
                        recursive(
                                record,
                                "offresrecues",
                                "offresRecues"
                        )
                ),

                text(
                        recursive(
                                record,
                                "idaccordcadre",
                                "idAccordCadre"
                        )
                ),

                text(
                        recursive(
                                record,
                                "lieuexecution_code",
                                "lieuExecutionCode"
                        )
                ),

                text(
                        recursive(
                                record,
                                "lieuexecution_typecode",
                                "lieuExecutionTypeCode"
                        )
                ),

                text(
                        recursive(
                                record,
                                "lieuexecution_nom",
                                "lieuExecutionNom"
                        )
                ),

                text(
                        recursive(
                                record,
                                "considerationssociales",
                                "considerationsSociales"
                        )
                ),

                text(
                        recursive(
                                record,
                                "considerationsenvironnementales",
                                "considerationsEnvironnementales"
                        )
                ),

                text(
                        recursive(
                                record,
                                "modalitesexecution",
                                "modalitesExecution"
                        )
                ),

                text(
                        recursive(
                                record,
                                "typegroupementoperateurs",
                                "typeGroupementOperateurs"
                        )
                ),

                text(
                        recursive(
                                record,
                                "soustraitancedeclaree",
                                "sousTraitanceDeclaree"
                        )
                ),

                text(
                        recursive(
                                record,
                                "source"
                        )
                ),

                suppliers(record)
        );
    }

    private List<DecpSupplierData> suppliers(
            Object record
    ) {

        Map<String, DecpSupplierData> unique =
                new LinkedHashMap<>();

        for (int index = 1; index <= 5; index++) {

            String identifier =
                    text(
                            recursive(
                                    record,
                                    "titulaire_id_" + index
                            )
                    );

            String identifierType =
                    text(
                            recursive(
                                    record,
                                    "titulaire_typeidentifiant_" + index
                            )
                    );

            String legalName =
                    text(
                            recursive(
                                    record,
                                    "titulaire_denominationsociale_" + index
                            )
                    );

            addSupplier(
                    unique,
                    identifier,
                    identifierType,
                    legalName
            );
        }

        Object nested =
                recursive(
                        record,
                        "titulaires"
                );

        if (nested instanceof List<?> values) {

            for (Object value : values) {

                addSupplier(
                        unique,

                        text(
                                recursive(
                                        value,
                                        "id"
                                )
                        ),

                        text(
                                recursive(
                                        value,
                                        "typeidentifiant",
                                        "typeIdentifiant"
                                )
                        ),

                        text(
                                recursive(
                                        value,
                                        "denominationsociale",
                                        "denominationSociale"
                                )
                        )
                );
            }
        }

        return List.copyOf(
                unique.values()
        );
    }

    private void addSupplier(
            Map<String, DecpSupplierData> unique,
            String identifier,
            String identifierType,
            String legalName
    ) {

        if (
                identifier == null
                &&
                legalName == null
        ) {
            return;
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
                new DecpSupplierData(
                        identifier,
                        identifierType,
                        legalName
                )
        );
    }

    private Object direct(
            Object root,
            String... keys
    ) {

        if (!(root instanceof Map<?, ?> map)) {
            return null;
        }

        for (String key : keys) {

            Object result =
                    mapValue(map, key);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private Object recursive(
            Object node,
            String... keys
    ) {

        for (String key : keys) {

            Object result =
                    recursiveOne(
                            node,
                            normalizeKey(key)
                    );

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private Object recursiveOne(
            Object node,
            String wanted
    ) {

        if (node instanceof Map<?, ?> map) {

            for (
                    Map.Entry<?, ?> entry :
                    map.entrySet()
            ) {

                if (
                        entry.getKey() != null
                        &&
                        normalizeKey(
                                entry.getKey()
                                        .toString()
                        )
                                .equals(wanted)
                ) {
                    return entry.getValue();
                }
            }

            for (Object value : map.values()) {

                Object result =
                        recursiveOne(
                                value,
                                wanted
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        if (node instanceof List<?> values) {

            for (Object value : values) {

                Object result =
                        recursiveOne(
                                value,
                                wanted
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private Object mapValue(
            Map<?, ?> map,
            String key
    ) {

        String normalized =
                normalizeKey(key);

        for (
                Map.Entry<?, ?> entry :
                map.entrySet()
        ) {

            if (
                    entry.getKey() != null
                    &&
                    normalizeKey(
                            entry.getKey()
                                    .toString()
                    )
                            .equals(normalized)
            ) {
                return entry.getValue();
            }
        }

        return null;
    }

    private String normalizeKey(
            String value
    ) {

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                );
    }

    private List<?> asList(Object value) {

        return value instanceof List<?> list
                ? list
                : List.of();
    }

    private String text(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof List<?> values) {

            return values.isEmpty()
                    ? null
                    : text(values.getFirst());
        }

        String cleaned =
                value
                        .toString()
                        .trim()
                        .replaceAll("\\s+", " ");

        return cleaned.isBlank()
                ? null
                : cleaned;
    }

    private LocalDate date(String value) {

        if (value == null) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {

            if (value.length() >= 10) {

                try {
                    return LocalDate.parse(
                            value.substring(0, 10)
                    );
                } catch (
                        DateTimeParseException ignoredAgain
                ) {
                    return null;
                }
            }

            return null;
        }
    }

    private Integer integer(Object value) {

        String cleaned = text(value);

        if (cleaned == null) {
            return null;
        }

        cleaned =
                cleaned.replaceAll(
                        "[^0-9\\-]",
                        ""
                );

        if (cleaned.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(cleaned);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private BigDecimal decimal(Object value) {

        String cleaned = text(value);

        if (cleaned == null) {
            return null;
        }

        cleaned =
                cleaned
                        .replace(" ", "")
                        .replace(",", ".")
                        .replaceAll(
                                "[^0-9.\\-]",
                                ""
                        );

        if (cleaned.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private long longValue(
            Object value,
            long fallback
    ) {

        String cleaned = text(value);

        if (cleaned == null) {
            return fallback;
        }

        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String normalizeCpv(String value) {

        if (value == null) {
            return null;
        }

        String digits =
                value.replaceAll(
                        "[^0-9]",
                        ""
                );

        return digits.length() >= 8
                ? digits.substring(0, 8)
                : null;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
