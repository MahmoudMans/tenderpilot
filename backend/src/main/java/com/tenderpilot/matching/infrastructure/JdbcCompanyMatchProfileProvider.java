package com.tenderpilot.matching.infrastructure;

import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.matching.application.CompanyMatchProfileProvider;
import com.tenderpilot.matching.domain.CompanyMatchProfile;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class JdbcCompanyMatchProfileProvider
        implements CompanyMatchProfileProvider {

    private static final Pattern CPV =
            Pattern.compile(
                    "(?<!\\d)(\\d{8})(?!\\d)"
            );


    private final JdbcTemplate jdbcTemplate;


    public JdbcCompanyMatchProfileProvider(
            JdbcTemplate jdbcTemplate
    ) {

        this.jdbcTemplate =
                jdbcTemplate;
    }


    @Override
    public CompanyMatchProfile load(
            UUID organizationId
    ) {

        List<Map<String, Object>> rows =
                jdbcTemplate
                        .queryForList(
                                """
                                select *
                                from company_profiles
                                where organization_id = ?
                                """,
                                organizationId
                        );


        if (
                rows.isEmpty()
        ) {

            throw new ResourceNotFoundException(
                    "Company profile is required before opportunity matching"
            );
        }


        Map<String, Object> profile =
                rows.getFirst();


        UUID profileId =
                uuid(
                        value(
                                profile,
                                "id"
                        )
                );


        if (
                profileId == null
        ) {

            throw new IllegalStateException(
                    "Company profile identifier is missing"
            );
        }


        List<String> capabilities =
                values(
                        "company_capabilities",
                        profileId,
                        List.of(
                                "capability",
                                "value",
                                "name",
                                "label"
                        )
                );


        List<String> targetCpvs =
                cpvValues(
                        profileId
                );


        List<String> buyerTypes =
                values(
                        "company_preferred_buyer_types",
                        profileId,
                        List.of(
                                "buyer_type",
                                "value",
                                "type"
                        )
                );


        List<String> exclusions =
                values(
                        "company_excluded_keywords",
                        profileId,
                        List.of(
                                "keyword",
                                "value",
                                "term"
                        )
                );


        List<CompanyMatchProfile.OperatingRegion>
                regions =
                regionValues(
                        profileId
                );


        return new CompanyMatchProfile(
                organizationId,

                capabilities,

                regions,

                targetCpvs,

                buyerTypes,

                exclusions,

                decimal(
                        value(
                                profile,
                                "preferred_contract_min_value",
                                "preferred_contract_min",
                                "preferred_min_contract_value",
                                "minimum_contract_value",
                                "contract_min_value"
                        )
                ),

                decimal(
                        value(
                                profile,
                                "preferred_contract_max_value",
                                "preferred_contract_max",
                                "preferred_max_contract_value",
                                "maximum_contract_value",
                                "contract_max_value"
                        )
                ),

                string(
                        value(
                                profile,
                                "preferred_contract_currency",
                                "preferred_currency",
                                "contract_currency",
                                "currency"
                        )
                ),

                integer(
                        value(
                                profile,
                                "minimum_notice_days",
                                "minimum_notice_period_days",
                                "min_notice_days",
                                "notice_days"
                        )
                )
        );
    }


    private List<String> values(
            String table,
            UUID profileId,
            List<String> preferredColumns
    ) {

        List<Map<String, Object>> rows =
                jdbcTemplate
                        .queryForList(
                                "select * from "
                                        +
                                        table
                                        +
                                        " where profile_id = ? order by position",
                                profileId
                        );


        Set<String> result =
                new LinkedHashSet<>();


        for (
                Map<String, Object> row :
                rows
        ) {

            String resolved =
                    null;


            for (
                    String column :
                    preferredColumns
            ) {

                resolved =
                        string(
                                value(
                                        row,
                                        column
                                )
                        );


                if (
                        resolved != null
                ) {
                    break;
                }
            }


            if (
                    resolved == null
            ) {

                resolved =
                        firstMeaningfulString(
                                row
                        );
            }


            if (
                    resolved != null
            ) {

                result.add(
                        resolved
                );
            }
        }


        return List.copyOf(
                result
        );
    }


    private List<String> cpvValues(
            UUID profileId
    ) {

        List<Map<String, Object>> rows =
                jdbcTemplate
                        .queryForList(
                                """
                                select *
                                from company_target_cpvs
                                where profile_id = ?
                                order by position
                                """,
                                profileId
                        );


        Set<String> cpvs =
                new LinkedHashSet<>();


        for (
                Map<String, Object> row :
                rows
        ) {

            for (
                    Object raw :
                    row.values()
            ) {

                if (
                        raw == null
                ) {
                    continue;
                }


                Matcher matcher =
                        CPV.matcher(
                                raw.toString()
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
        }


        return List.copyOf(
                cpvs
        );
    }


    private List<CompanyMatchProfile.OperatingRegion>
    regionValues(
            UUID profileId
    ) {

        List<Map<String, Object>> rows =
                jdbcTemplate
                        .queryForList(
                                """
                                select *
                                from company_operating_regions
                                where profile_id = ?
                                order by position
                                """,
                                profileId
                        );


        List<CompanyMatchProfile.OperatingRegion>
                result =
                new ArrayList<>();


        for (
                Map<String, Object> row :
                rows
        ) {

            String country =
                    string(
                            value(
                                    row,
                                    "country_code",
                                    "country",
                                    "countrycode"
                            )
                    );


            String regionCode =
                    string(
                            value(
                                    row,
                                    "region_code",
                                    "regioncode",
                                    "code"
                            )
                    );


            String regionName =
                    string(
                            value(
                                    row,
                                    "region_name",
                                    "regionname",
                                    "name",
                                    "value"
                            )
                    );


            if (
                    country == null
                    &&
                    regionCode == null
                    &&
                    regionName == null
            ) {

                regionName =
                        firstMeaningfulString(
                                row
                        );
            }


            if (
                    country != null
                    ||
                    regionCode != null
                    ||
                    regionName != null
            ) {

                result.add(
                        new CompanyMatchProfile
                                .OperatingRegion(
                                        country,
                                        regionCode,
                                        regionName
                                )
                );
            }
        }


        return List.copyOf(
                result
        );
    }


    private String firstMeaningfulString(
            Map<String, Object> row
    ) {

        Set<String> ignored =
                Set.of(
                        "id",
                        "profileid",
                        "position",
                        "organizationid",
                        "createdat",
                        "updatedat"
                );


        for (
                Map.Entry<String, Object> entry :
                row.entrySet()
        ) {

            if (
                    ignored.contains(
                            normalizeKey(
                                    entry.getKey()
                            )
                    )
            ) {
                continue;
            }


            String candidate =
                    string(
                            entry.getValue()
                    );


            if (
                    candidate != null
            ) {

                return candidate;
            }
        }


        return null;
    }


    private Object value(
            Map<String, Object> row,
            String... candidates
    ) {

        for (
                String candidate :
                candidates
        ) {

            String normalized =
                    normalizeKey(
                            candidate
                    );


            for (
                    Map.Entry<String, Object> entry :
                    row.entrySet()
            ) {

                if (
                        normalizeKey(
                                entry.getKey()
                        )
                                .equals(
                                        normalized
                                )
                ) {

                    return entry.getValue();
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


    private UUID uuid(
            Object value
    ) {

        if (
                value instanceof UUID uuid
        ) {

            return uuid;
        }


        if (
                value == null
        ) {

            return null;
        }


        try {

            return UUID.fromString(
                    value.toString()
            );

        } catch (
                IllegalArgumentException exception
        ) {

            return null;
        }
    }


    private String string(
            Object value
    ) {

        if (
                value == null
        ) {
            return null;
        }


        String result =
                value
                        .toString()
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return result.isBlank()
                ? null
                : result;
    }


    private BigDecimal decimal(
            Object value
    ) {

        if (
                value == null
        ) {
            return null;
        }


        if (
                value instanceof BigDecimal decimal
        ) {

            return decimal;
        }


        if (
                value instanceof Number number
        ) {

            return new BigDecimal(
                    number.toString()
            );
        }


        try {

            return new BigDecimal(
                    value
                            .toString()
                            .trim()
                            .replace(
                                    ",",
                                    "."
                            )
            );

        } catch (
                NumberFormatException exception
        ) {

            return null;
        }
    }


    private Integer integer(
            Object value
    ) {

        if (
                value instanceof Number number
        ) {

            return number.intValue();
        }


        if (
                value == null
        ) {

            return null;
        }


        try {

            return Integer.valueOf(
                    value.toString()
            );

        } catch (
                NumberFormatException exception
        ) {

            return null;
        }
    }

}
