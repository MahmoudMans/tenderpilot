package com.tenderpilot.datasource.ted;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class TedSearchResponseParser {

    public TedSearchPage parse(
            Object root
    ) {

        List<?> rawNotices =
                locateNoticeArray(
                        root
                );


        List<TedSearchNotice> notices =

                rawNotices
                        .stream()
                        .map(
                                this::parseNotice
                        )
                        .filter(
                                notice ->
                                        notice.externalId()
                                                !=
                                                null
                        )
                        .toList();


        long total =
                firstLong(

                        findValue(

                                root,

                                "total",
                                "totalCount",
                                "totalNoticeCount",
                                "totalNotices",
                                "totalResults"

                        ),

                        notices.size()

                );


        return new TedSearchPage(

                total,

                notices

        );

    }


    private TedSearchNotice parseNotice(
            Object item
    ) {

        String externalId =

                firstText(

                        findValue(

                                item,

                                "publication-number",
                                "publicationNumber",
                                "ND"

                        )

                );


        String externalVersion =

                firstText(

                        findValue(

                                item,

                                "notice-version",
                                "noticeVersion"

                        )

                );


        String noticeType =

                firstText(

                        findValue(

                                item,

                                "notice-type",
                                "noticeType"

                        )

                );


        String title =

                firstText(

                        findValue(

                                item,

                                "title-proc",
                                "notice-title",
                                "noticeTitle",
                                "title"

                        )

                );


        String description =

                firstText(

                        findValue(

                                item,

                                "description-proc",
                                "description"

                        )

                );


        LocalDate publicationDate =

                firstDate(

                        findValue(

                                item,

                                "publication-date",
                                "publicationDate",
                                "PD"

                        )

                );


        TedBuyerData buyer =

                new TedBuyerData(

                        firstText(
                                findValue(
                                        item,
                                        "buyer-name",
                                        "buyerName",
                                        "AU"
                                )
                        ),

                        firstText(
                                findValue(
                                        item,
                                        "buyer-identifier",
                                        "buyerIdentifier",
                                        "BI"
                                )
                        ),

                        firstText(
                                findValue(
                                        item,
                                        "buyer-country",
                                        "buyerCountry",
                                        "CY"
                                )
                        ),

                        firstText(
                                findValue(
                                        item,
                                        "buyer-city",
                                        "buyerCity",
                                        "TW"
                                )
                        ),

                        firstText(
                                findValue(
                                        item,
                                        "buyer-internet-address",
                                        "buyerInternetAddress",
                                        "IA"
                                )
                        ),

                        firstText(
                                findValue(
                                        item,
                                        "buyer-legal-type",
                                        "buyerLegalType",
                                        "AA"
                                )
                        )

                );


        String procedureType =

                firstText(

                        findValue(

                                item,

                                "procedure-type",
                                "procedureType",
                                "PR"

                        )

                );


        String contractNature =

                firstText(

                        findValue(

                                item,

                                "contract-nature-main-proc",
                                "contract-nature",
                                "contractNature",
                                "NC"

                        )

                );


        List<LocalDate> deadlineDates =

                textValues(

                        findValue(

                                item,

                                "deadline-receipt-tender-date-lot",
                                "deadline-date-lot",
                                "deadline"

                        )

                )
                        .stream()
                        .map(
                                this::parseDate
                        )
                        .filter(
                                value ->
                                        value != null
                        )
                        .distinct()
                        .sorted()
                        .toList();


        LocalTime deadlineTime =

                firstTime(

                        findValue(

                                item,

                                "deadline-receipt-tender-time-lot",
                                "deadline-time-lot"

                        )

                );


        BigDecimal estimatedValue =

                firstDecimal(

                        findValue(

                                item,

                                "estimated-value-proc",
                                "estimatedValueProc",
                                "total-value",
                                "TV"

                        )

                );


        String currency =

                firstText(

                        findValue(

                                item,

                                "estimated-value-cur-proc",
                                "estimatedValueCurrency",
                                "total-value-cur",
                                "TV_CUR"

                        )

                );


        List<String> cpvs =

                textValues(

                        findValue(

                                item,

                                "classification-cpv",
                                "classificationCpv",
                                "PC"

                        )

                )
                        .stream()
                        .map(
                                this::extractCpv
                        )
                        .filter(
                                value ->
                                        value != null
                        )
                        .distinct()
                        .toList();


        List<TedLocationData> locations =

                buildLocations(

                        textValues(
                                findValue(
                                        item,
                                        "place-of-performance-country-proc"
                                )
                        ),

                        textValues(
                                findValue(
                                        item,
                                        "place-of-performance-subdiv-proc"
                                )
                        ),

                        textValues(
                                findValue(
                                        item,
                                        "place-of-performance-city-proc"
                                )
                        )

                );


        boolean frameworkAgreement =

                signalEnabled(

                        findValue(

                                item,

                                "framework-agreement-lot"

                        )

                );


        boolean electronicSubmission =

                signalEnabled(

                        findValue(

                                item,

                                "electronic-submission-lot"

                        )

                );


        String sourceUrl =
                chooseSourceUrl(
                        item
                );


        return new TedSearchNotice(

                clean(
                        externalId
                ),

                clean(
                        externalVersion
                ),

                clean(
                        noticeType
                ),

                clean(
                        title
                ),

                clean(
                        description
                ),

                publicationDate,

                buyer,

                clean(
                        procedureType
                ),

                clean(
                        contractNature
                ),

                deadlineDates,

                deadlineTime,

                estimatedValue,

                clean(
                        currency
                ),

                cpvs,

                locations,

                frameworkAgreement,

                electronicSubmission,

                sourceUrl

        );

    }


    private List<TedLocationData> buildLocations(

            List<String> countries,

            List<String> regions,

            List<String> cities

    ) {

        int size =

                Math.max(

                        countries.size(),

                        Math.max(
                                regions.size(),
                                cities.size()
                        )

                );


        if (
                size == 0
        ) {

            return List.of();

        }


        List<TedLocationData> result =
                new ArrayList<>();


        Set<String> seen =
                new LinkedHashSet<>();


        for (
                int index = 0;
                index < size;
                index++
        ) {

            String country =
                    itemAt(
                            countries,
                            index
                    );


            String region =
                    itemAt(
                            regions,
                            index
                    );


            String city =
                    itemAt(
                            cities,
                            index
                    );


            String key =

                    nullSafe(
                            country
                    )
                            +
                            "|"
                            +
                            nullSafe(
                                    region
                            )
                            +
                            "|"
                            +
                            nullSafe(
                                    city
                            );


            if (
                    seen.add(
                            key
                    )
            ) {

                result.add(

                        new TedLocationData(

                                clean(
                                        country
                                ),

                                clean(
                                        region
                                ),

                                clean(
                                        city
                                )

                        )

                );

            }

        }


        return List.copyOf(
                result
        );

    }


    private String itemAt(

            List<String> values,

            int index

    ) {

        if (
                index >=
                values.size()
        ) {

            return null;

        }


        return values.get(
                index
        );

    }


    private List<?> locateNoticeArray(
            Object root
    ) {

        Object known =

                findValue(

                        root,

                        "notices",
                        "results",
                        "items"

                );


        if (
                known instanceof List<?> list
        ) {

            return list;

        }


        List<?> discovered =
                firstObjectList(
                        root
                );


        return discovered == null
                ? List.of()
                : discovered;

    }


    private List<?> firstObjectList(
            Object node
    ) {

        if (
                node instanceof List<?> list
        ) {

            boolean hasMap =

                    list.stream()
                            .anyMatch(
                                    value ->
                                            value instanceof Map<?, ?>
                            );


            if (
                    hasMap
            ) {

                return list;

            }


            for (
                    Object value :
                    list
            ) {

                List<?> result =
                        firstObjectList(
                                value
                        );


                if (
                        result != null
                ) {

                    return result;

                }

            }


            return null;

        }


        if (
                node instanceof Map<?, ?> map
        ) {

            for (
                    Object value :
                    map.values()
            ) {

                List<?> result =
                        firstObjectList(
                                value
                        );


                if (
                        result != null
                ) {

                    return result;

                }

            }

        }


        return null;

    }


    private Object findValue(

            Object node,

            String... keys

    ) {

        Set<String> normalizedKeys =
                new LinkedHashSet<>();


        for (
                String key :
                keys
        ) {

            normalizedKeys.add(
                    normalizeKey(
                            key
                    )
            );

        }


        return findValueRecursive(

                node,

                normalizedKeys

        );

    }


    private Object findValueRecursive(

            Object node,

            Set<String> normalizedKeys

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
                        normalizedKeys.contains(
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

                        findValueRecursive(

                                value,

                                normalizedKeys

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

                        findValueRecursive(

                                value,

                                normalizedKeys

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
                .distinct()
                .toList();

    }


    private void collectText(

            Object value,

            List<String> result

    ) {

        if (
                value == null
        ) {

            return;

        }


        if (
                value instanceof String string
        ) {

            result.add(
                    string
            );

            return;

        }


        if (
                value instanceof Number
                ||
                value instanceof Boolean
        ) {

            result.add(
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
                        result
                );

            }


            return;

        }


        if (
                value instanceof Map<?, ?> map
        ) {

            Object english =

                    firstMapValue(

                            map,

                            "eng",
                            "en",
                            "ENG",
                            "EN"

                    );


            if (
                    english != null
            ) {

                collectText(
                        english,
                        result
                );


                for (
                        Object item :
                        map.values()
                ) {

                    if (
                            item != english
                    ) {

                        collectText(
                                item,
                                result
                        );

                    }

                }


                return;

            }


            for (
                    Object item :
                    map.values()
            ) {

                collectText(
                        item,
                        result
                );

            }

        }

    }


    private Object firstMapValue(

            Map<?, ?> map,

            String... keys

    ) {

        for (
                String key :
                keys
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
                        entry
                                .getKey()
                                .toString()
                                .equalsIgnoreCase(
                                        key
                                )
                ) {

                    return entry.getValue();

                }

            }

        }


        return null;

    }


    private LocalDate firstDate(
            Object value
    ) {

        for (
                String text :
                textValues(
                        value
                )
        ) {

            LocalDate parsed =
                    parseDate(
                            text
                    );


            if (
                    parsed != null
            ) {

                return parsed;

            }

        }


        return null;

    }


    private LocalDate parseDate(
            String value
    ) {

        if (
                value == null
        ) {

            return null;

        }


        String cleaned =
                value.trim();


        List<DateTimeFormatter> formatters =

                List.of(

                        DateTimeFormatter.ISO_LOCAL_DATE,

                        DateTimeFormatter.BASIC_ISO_DATE

                );


        for (
                DateTimeFormatter formatter :
                formatters
        ) {

            try {

                return LocalDate.parse(

                        cleaned,

                        formatter

                );

            } catch (
                    DateTimeParseException ignored
            ) {

                // Try the next format.

            }

        }


        try {

            return OffsetDateTime
                    .parse(
                            cleaned
                    )
                    .toLocalDate();

        } catch (
                DateTimeParseException ignored
        ) {

            return null;

        }

    }


    private LocalTime firstTime(
            Object value
    ) {

        for (
                String text :
                textValues(
                        value
                )
        ) {

            try {

                return LocalTime.parse(
                        text
                );

            } catch (
                    DateTimeParseException ignored
            ) {

                // Continue.

            }

        }


        return null;

    }


    private BigDecimal firstDecimal(
            Object value
    ) {

        for (
                String text :
                textValues(
                        value
                )
        ) {

            try {

                return new BigDecimal(

                        text
                                .replace(
                                        " ",
                                        ""
                                )
                                .replace(
                                        ",",
                                        "."
                                )

                );

            } catch (
                    NumberFormatException ignored
            ) {

                // Continue.

            }

        }


        return null;

    }


    private String extractCpv(
            String value
    ) {

        if (
                value == null
        ) {

            return null;

        }


        String digits =
                value.replaceAll(
                        "[^0-9]",
                        ""
                );


        if (
                digits.length()
                        <
                        8
        ) {

            return null;

        }


        String candidate =
                digits.substring(
                        0,
                        8
                );


        return candidate.matches(
                "\\d{8}"
        )
                ? candidate
                : null;

    }


    private boolean signalEnabled(
            Object value
    ) {

        List<String> values =
                textValues(
                        value
                );


        for (
                String candidate :
                values
        ) {

            String normalized =

                    candidate
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );


            if (
                    normalized.equals(
                            "false"
                    )
                    ||
                    normalized.equals(
                            "no"
                    )
                    ||
                    normalized.equals(
                            "none"
                    )
                    ||
                    normalized.equals(
                            "not-applicable"
                    )
                    ||
                    normalized.equals(
                            "not applicable"
                    )
            ) {

                continue;

            }


            if (
                    normalized.equals(
                            "true"
                    )
                    ||
                    normalized.equals(
                            "yes"
                    )
                    ||
                    normalized.contains(
                            "framework"
                    )
                    ||
                    normalized.startsWith(
                            "fa-"
                    )
                    ||
                    normalized.contains(
                            "allowed"
                    )
                    ||
                    normalized.contains(
                            "required"
                    )
            ) {

                return true;

            }

        }


        return false;

    }


    private String chooseSourceUrl(
            Object item
    ) {

        List<String> urls =
                new ArrayList<>();


        collectUrls(
                item,
                urls
        );


        for (
                String url :
                urls
        ) {

            String lower =
                    url.toLowerCase(
                            Locale.ROOT
                    );


            if (
                    lower.contains(
                            "ted.europa.eu"
                    )
                    &&
                    !lower.endsWith(
                            ".xml"
                    )
                    &&
                    !lower.endsWith(
                            ".pdf"
                    )
                    &&
                    !lower.contains(
                            "/api/"
                    )
            ) {

                return url;

            }

        }


        return urls.isEmpty()
                ? null
                : urls.getFirst();

    }


    private void collectUrls(

            Object node,

            List<String> urls

    ) {

        if (
                node instanceof String string
        ) {

            if (
                    string.startsWith(
                            "https://"
                    )
                    ||
                    string.startsWith(
                            "http://"
                    )
            ) {

                urls.add(
                        string
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

                collectUrls(
                        value,
                        urls
                );

            }


            return;

        }


        if (
                node instanceof Map<?, ?> map
        ) {

            for (
                    Object value :
                    map.values()
            ) {

                collectUrls(
                        value,
                        urls
                );

            }

        }

    }


    private long firstLong(

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

}
