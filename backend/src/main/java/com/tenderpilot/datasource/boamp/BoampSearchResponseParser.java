package com.tenderpilot.datasource.boamp;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class BoampSearchResponseParser {

    private static final Pattern RAW_CPV_PATTERN =

            Pattern.compile(

                    "(?i)(?:cpv|classification)[^0-9]{0,120}([0-9]{8})"

            );


    public BoampSearchPage parse(
            Object root
    ) {

        List<?> rawResults =

                listValue(

                        findDirect(

                                root,

                                "results",
                                "records"

                        )

                );


        List<BoampNotice> notices =

                rawResults
                        .stream()
                        .map(
                                this::unwrapRecord
                        )
                        .map(
                                this::parseRecord
                        )
                        .filter(
                                notice ->
                                        notice.externalId()
                                                !=
                                                null
                        )
                        .toList();


        long total =

                longValue(

                        findDirect(

                                root,

                                "total_count",
                                "totalCount",
                                "nhits"

                        ),

                        notices.size()

                );


        return new BoampSearchPage(

                total,

                notices

        );

    }


    private Object unwrapRecord(
            Object raw
    ) {

        if (
                !(raw instanceof Map<?, ?> map)
        ) {

            return raw;

        }


        Object record =
                directMapValue(
                        map,
                        "record"
                );


        if (
                record instanceof Map<?, ?> recordMap
        ) {

            Object fields =
                    directMapValue(
                            recordMap,
                            "fields"
                    );


            if (
                    fields instanceof Map<?, ?>
            ) {

                return fields;

            }


            return record;

        }


        Object fields =
                directMapValue(
                        map,
                        "fields"
                );


        return fields instanceof Map<?, ?>
                ? fields
                : raw;

    }


    private BoampNotice parseRecord(
            Object record
    ) {

        String idWeb =
                firstText(

                        findRecursive(

                                record,

                                "idweb"

                        )

                );


        String id =
                firstText(

                        findRecursive(

                                record,

                                "id"

                        )

                );


        String externalId =

                clean(
                        idWeb
                );


        if (
                externalId == null
        ) {

            externalId =
                    clean(
                            id
                    );

        }


        String object =

                firstText(

                        findRecursive(

                                record,

                                "objet",
                                "object"

                        )

                );


        LocalDate publicationDate =

                localDate(

                        firstText(

                                findRecursive(

                                        record,

                                        "dateparution"

                                )

                        )

                );


        Instant deadline =

                instant(

                        firstText(

                                findRecursive(

                                        record,

                                        "datelimitereponse"

                                )

                        )

                );


        String buyerName =

                firstText(

                        findRecursive(

                                record,

                                "nomacheteur"

                        )

                );


        String procedure =

                firstNonBlank(

                        firstText(
                                findRecursive(
                                        record,
                                        "procedure_libelle"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "procedure_categorise"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "type_procedure"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "soustype_procedure"
                                )
                        )

                );


        String nature =

                firstNonBlank(

                        firstText(
                                findRecursive(
                                        record,
                                        "type_marche_facette"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "type_marche"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "nature_categorise_libelle"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "nature_libelle"
                                )
                        )

                );


        String noticeType =

                firstNonBlank(

                        firstText(
                                findRecursive(
                                        record,
                                        "type_avis"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "famille_libelle"
                                )
                        )

                );


        String sourceSchema =

                firstText(

                        findRecursive(

                                record,

                                "source_schema"

                        )

                );


        String sourceUrl =

                firstText(

                        findRecursive(

                                record,

                                "url_avis"

                        )

                );


        String department =

                firstNonBlank(

                        firstText(
                                findRecursive(
                                        record,
                                        "code_departement_prestation"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "code_departement"
                                )
                        )

                );


        List<String> cpvCodes =
                cpvCodes(
                        record
                );


        BigDecimal estimatedValue =

                decimalValue(

                        findRecursive(

                                record,

                                "montant_estime",
                                "montant",
                                "valeur_estimee",
                                "estimated_value",
                                "estimatedvalue"

                        )

                );


        String currency =

                firstNonBlank(

                        firstText(
                                findRecursive(
                                        record,
                                        "devise"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "currency"
                                )
                        ),

                        firstText(
                                findRecursive(
                                        record,
                                        "monnaie"
                                )
                        )

                );


        return new BoampNotice(

                clean(
                        externalId
                ),

                clean(
                        id
                ),

                clean(
                        object
                ),

                publicationDate,

                deadline,

                clean(
                        buyerName
                ),

                clean(
                        procedure
                ),

                clean(
                        nature
                ),

                clean(
                        noticeType
                ),

                clean(
                        sourceSchema
                ),

                clean(
                        sourceUrl
                ),

                clean(
                        department
                ),

                cpvCodes,

                estimatedValue,

                normalizeCurrency(
                        currency
                )

        );

    }


    private List<String> cpvCodes(
            Object record
    ) {

        Set<String> cpvs =
                new LinkedHashSet<>();


        collectCpvValues(

                record,

                cpvs

        );


        Object rawDonnees =

                findRecursive(

                        record,

                        "donnees",
                        "DONNEES"

                );


        for (
                String raw :
                textValues(
                        rawDonnees
                )
        ) {

            Matcher matcher =

                    RAW_CPV_PATTERN
                            .matcher(
                                    raw
                            );


            while (
                    matcher.find()
            ) {

                cpvs.add(
                        matcher.group(
                                1
                        )
                );

            }

        }


        return List.copyOf(
                cpvs
        );

    }


    private void collectCpvValues(

            Object node,

            Set<String> cpvs

    ) {

        if (
                node instanceof Map<?, ?> map
        ) {

            for (
                    Map.Entry<?, ?> entry :
                    map.entrySet()
            ) {

                String key =

                        entry.getKey()
                                ==
                                null

                                ? ""

                                : normalizeKey(
                                        entry
                                                .getKey()
                                                .toString()
                                );


                if (
                        key.contains(
                                "cpv"
                        )
                ) {

                    for (
                            String value :
                            textValues(
                                    entry.getValue()
                            )
                    ) {

                        String digits =

                                value.replaceAll(
                                        "[^0-9]",
                                        ""
                                );


                        if (
                                digits.length()
                                        >=
                                        8
                        ) {

                            cpvs.add(
                                    digits.substring(
                                            0,
                                            8
                                    )
                            );

                        }

                    }

                }


                collectCpvValues(

                        entry.getValue(),

                        cpvs

                );

            }


            return;

        }


        if (
                node instanceof List<?> list
        ) {

            for (
                    Object value :
                    list
            ) {

                collectCpvValues(
                        value,
                        cpvs
                );

            }

        }

    }


    private Object findDirect(

            Object node,

            String... keys

    ) {

        if (
                !(node instanceof Map<?, ?> map)
        ) {

            return null;

        }


        for (
                String key :
                keys
        ) {

            Object value =
                    directMapValue(
                            map,
                            key
                    );


            if (
                    value != null
            ) {

                return value;

            }

        }


        return null;

    }


    private Object findRecursive(

            Object node,

            String... keys

    ) {

        Set<String> wanted =
                new LinkedHashSet<>();


        for (
                String key :
                keys
        ) {

            wanted.add(
                    normalizeKey(
                            key
                    )
            );

        }


        return findRecursive0(

                node,

                wanted

        );

    }


    private Object findRecursive0(

            Object node,

            Set<String> wanted

    ) {

        if (
                node instanceof Map<?, ?> map
        ) {

            for (
                    Map.Entry<?, ?> entry :
                    map.entrySet()
            ) {

                if (
                        entry.getKey()
                                !=
                                null
                        &&
                        wanted.contains(
                                normalizeKey(
                                        entry
                                                .getKey()
                                                .toString()
                                )
                        )
                ) {

                    return entry.getValue();

                }

            }


            for (
                    Object value :
                    map.values()
            ) {

                Object found =

                        findRecursive0(

                                value,

                                wanted

                        );


                if (
                        found != null
                ) {

                    return found;

                }

            }

        }


        if (
                node instanceof List<?> list
        ) {

            for (
                    Object value :
                    list
            ) {

                Object found =

                        findRecursive0(

                                value,

                                wanted

                        );


                if (
                        found != null
                ) {

                    return found;

                }

            }

        }


        return null;

    }


    private Object directMapValue(

            Map<?, ?> map,

            String key

    ) {

        String normalized =
                normalizeKey(
                        key
                );


        for (
                Map.Entry<?, ?> entry :
                map.entrySet()
        ) {

            if (
                    entry.getKey()
                            !=
                            null
                    &&
                    normalizeKey(
                            entry
                                    .getKey()
                                    .toString()
                    )
                            .equals(
                                    normalized
                            )
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
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                );

    }


    private List<?> listValue(
            Object value
    ) {

        return value instanceof List<?> list
                ? list
                : List.of();

    }


    private String firstText(
            Object value
    ) {

        List<String> values =
                textValues(
                        value
                );


        return values.isEmpty()
                ? null
                : values.getFirst();

    }


    private List<String> textValues(
            Object value
    ) {

        List<String> result =
                new ArrayList<>();


        collectText(

                value,

                result

        );


        return result
                .stream()
                .map(
                        this::clean
                )
                .filter(
                        item ->
                                item != null
                )
                .toList();

    }


    private void collectText(

            Object value,

            List<String> target

    ) {

        if (
                value == null
        ) {

            return;

        }


        if (
                value instanceof String text
        ) {

            target.add(
                    text
            );

            return;

        }


        if (
                value instanceof Number
                ||
                value instanceof Boolean
        ) {

            target.add(
                    value.toString()
            );

            return;

        }


        if (
                value instanceof List<?> list
        ) {

            for (
                    Object item :
                    list
            ) {

                collectText(
                        item,
                        target
                );

            }


            return;

        }


        if (
                value instanceof Map<?, ?> map
        ) {

            for (
                    Object item :
                    map.values()
            ) {

                collectText(
                        item,
                        target
                );

            }

        }

    }


    private LocalDate localDate(
            String value
    ) {

        if (
                value == null
        ) {

            return null;

        }


        try {

            return LocalDate.parse(
                    value
            );

        } catch (
                DateTimeParseException ignored
        ) {

            try {

                return OffsetDateTime
                        .parse(
                                value
                        )
                        .toLocalDate();

            } catch (
                    DateTimeParseException ignoredAgain
            ) {

                return null;

            }

        }

    }


    private Instant instant(
            String value
    ) {

        if (
                value == null
        ) {

            return null;

        }


        try {

            return Instant.parse(
                    value
            );

        } catch (
                DateTimeParseException ignored
        ) {

            // Continue.

        }


        try {

            return OffsetDateTime
                    .parse(
                            value
                    )
                    .toInstant();

        } catch (
                DateTimeParseException ignored
        ) {

            // Continue.

        }


        try {

            return LocalDateTime
                    .parse(
                            value
                    )
                    .atZone(
                            ZoneId.of(
                                    "Europe/Paris"
                            )
                    )
                    .toInstant();

        } catch (
                DateTimeParseException ignored
        ) {

            // Continue.

        }


        try {

            return LocalDate
                    .parse(
                            value
                    )
                    .atTime(
                            23,
                            59
                    )
                    .atZone(
                            ZoneId.of(
                                    "Europe/Paris"
                            )
                    )
                    .toInstant();

        } catch (
                DateTimeParseException ignored
        ) {

            return null;

        }

    }


    private BigDecimal decimalValue(
            Object value
    ) {

        String text =
                firstText(
                        value
                );


        if (
                text == null
        ) {

            return null;

        }


        String cleaned =

                text
                        .replace(
                                " ",
                                ""
                        )
                        .replace(
                                "€",
                                ""
                        )
                        .replace(
                                ",",
                                "."
                        )
                        .replaceAll(
                                "[^0-9.\\-]",
                                ""
                        );


        if (
                cleaned.isBlank()
        ) {

            return null;

        }


        try {

            return new BigDecimal(
                    cleaned
            );

        } catch (
                NumberFormatException exception
        ) {

            return null;

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


        if (
                upper.equals(
                        "€"
                )
        ) {

            return "EUR";

        }


        return upper.matches(
                "[A-Z]{3}"
        )
                ? upper
                : null;

    }


    private long longValue(

            Object value,

            long fallback

    ) {

        String text =
                firstText(
                        value
                );


        if (
                text == null
        ) {

            return fallback;

        }


        try {

            return Long.parseLong(
                    text
            );

        } catch (
                NumberFormatException exception
        ) {

            return fallback;

        }

    }


    private String firstNonBlank(
            String... values
    ) {

        for (
                String value :
                values
        ) {

            String cleaned =
                    clean(
                            value
                    );


            if (
                    cleaned != null
            ) {

                return cleaned;

            }

        }


        return null;

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

}
