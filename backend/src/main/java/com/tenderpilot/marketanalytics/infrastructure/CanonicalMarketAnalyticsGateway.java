package com.tenderpilot.marketanalytics.infrastructure;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.marketanalytics.application.MarketAnalyticsGateway;

import com.tenderpilot.marketanalytics.domain.MarketAnalyticsCapabilities;
import com.tenderpilot.marketanalytics.domain.MarketAwardObservation;
import com.tenderpilot.marketanalytics.domain.MarketCpvObservation;
import com.tenderpilot.marketanalytics.domain.MarketLocationObservation;
import com.tenderpilot.marketanalytics.domain.MarketQuery;
import com.tenderpilot.marketanalytics.domain.MarketSupplierObservation;
import com.tenderpilot.marketanalytics.domain.MarketTenderObservation;

import java.math.BigDecimal;

import java.sql.Date;
import java.sql.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class CanonicalMarketAnalyticsGateway
        implements MarketAnalyticsGateway {

    private final JdbcTemplate
            jdbcTemplate;


    private final MarketAnalyticsSchemaInspector
            schemaInspector;


    public CanonicalMarketAnalyticsGateway(
            JdbcTemplate jdbcTemplate,
            MarketAnalyticsSchemaInspector schemaInspector
    ) {

        this.jdbcTemplate =
                jdbcTemplate;

        this.schemaInspector =
                schemaInspector;
    }


    @Override
    public MarketAnalyticsCapabilities capabilities() {

        var schema =
                schemaInspector
                        .schema();


        var base =
                schema.base();


        return new MarketAnalyticsCapabilities(
                schema.countryAvailable(),

                schema.regionAvailable(),

                base.cpvAvailable(),

                base.awardsAvailable(),

                base.suppliersAvailable(),

                schema.tenderProcedure() != null,

                schema.tenderContractNature() != null,

                base.tenderPublicationDate() != null,

                base.tenderDeadline() != null
        );
    }


    @Override
    public List<MarketTenderObservation> tenders(
            MarketQuery query
    ) {

        MarketQuery safeQuery =
                query == null
                        ? new MarketQuery(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                        : query;


        var schema =
                schemaInspector
                        .schema();


        var base =
                schema.base();


        validateQueryCapabilities(
                safeQuery,
                schema
        );


        StringBuilder sql =
                new StringBuilder();


        sql.append(
                "select "
        )
                .append(
                        column(
                                "t",
                                base.tenderId()
                        )
                )
                .append(
                        " as tender_id, "
                )
                .append(
                        column(
                                "b",
                                base.buyerId()
                        )
                )
                .append(
                        " as buyer_id, "
                )
                .append(
                        column(
                                "b",
                                base.buyerName()
                        )
                )
                .append(
                        " as buyer_name, "
                )
                .append(
                        projection(
                                "b",
                                base.buyerCategory(),
                                "buyer_category"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                "b",
                                base.buyerCountry(),
                                "buyer_country"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        column(
                                "t",
                                base.tenderTitle()
                        )
                )
                .append(
                        " as tender_title, "
                )
                .append(
                        projection(
                                "t",
                                base.tenderStatus(),
                                "tender_status"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                "t",
                                schema.tenderProcedure(),
                                "tender_procedure"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                "t",
                                schema.tenderContractNature(),
                                "contract_nature"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                "t",
                                base.tenderPublicationDate(),
                                "publication_date"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                "t",
                                base.tenderDeadline(),
                                "submission_deadline"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                "t",
                                base.tenderMinValue(),
                                "estimated_min_value"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                "t",
                                base.tenderMaxValue(),
                                "estimated_max_value"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                "t",
                                base.tenderCurrency(),
                                "tender_currency"
                        )
                )
                .append(
                        " from "
                )
                .append(
                        q(
                                base.tenderTable()
                        )
                )
                .append(
                        " t join "
                )
                .append(
                        q(
                                base.buyerTable()
                        )
                )
                .append(
                        " b on "
                )
                .append(
                        column(
                                "t",
                                base.tenderBuyerId()
                        )
                )
                .append(
                        " = "
                )
                .append(
                        column(
                                "b",
                                base.buyerId()
                        )
                )
                .append(
                        " where 1 = 1"
                );


        List<Object> args =
                new ArrayList<>();


        if (
                safeQuery.query() != null
        ) {

            sql.append(
                    " and (lower("
            )
                    .append(
                            column(
                                    "t",
                                    base.tenderTitle()
                            )
                    )
                    .append(
                            ") like lower(?) or lower("
                    )
                    .append(
                            column(
                                    "b",
                                    base.buyerName()
                            )
                    )
                    .append(
                            ") like lower(?))"
                    );


            String contains =
                    "%"
                            +
                            safeQuery.query()
                            +
                            "%";


            args.add(
                    contains
            );

            args.add(
                    contains
            );
        }


        if (
                safeQuery.status() != null
        ) {

            sql.append(
                    " and upper("
            )
                    .append(
                            column(
                                    "t",
                                    base.tenderStatus()
                            )
                    )
                    .append(
                            ") = upper(?)"
                    );


            args.add(
                    safeQuery.status()
            );
        }


        if (
                safeQuery.dateFrom() != null
        ) {

            sql.append(
                    " and "
            )
                    .append(
                            column(
                                    "t",
                                    base.tenderPublicationDate()
                            )
                    )
                    .append(
                            " >= ?"
                    );


            args.add(
                    Date.valueOf(
                            safeQuery.dateFrom()
                    )
            );
        }


        if (
                safeQuery.dateTo() != null
        ) {

            sql.append(
                    " and "
            )
                    .append(
                            column(
                                    "t",
                                    base.tenderPublicationDate()
                            )
                    )
                    .append(
                            " <= ?"
                    );


            args.add(
                    Date.valueOf(
                            safeQuery.dateTo()
                    )
            );
        }


        appendCountryFilter(
                sql,
                args,
                safeQuery.country(),
                schema
        );


        appendRegionFilter(
                sql,
                args,
                safeQuery.region(),
                schema
        );


        appendCpvFilter(
                sql,
                args,
                safeQuery.cpvPrefix(),
                schema
        );


        if (
                base.tenderPublicationDate() != null
        ) {

            sql.append(
                    " order by "
            )
                    .append(
                            column(
                                    "t",
                                    base.tenderPublicationDate()
                            )
                    )
                    .append(
                            " desc, lower("
                    )
                    .append(
                            column(
                                    "t",
                                    base.tenderTitle()
                            )
                    )
                    .append(
                            ") asc"
                    );


        } else {

            sql.append(
                    " order by lower("
            )
                    .append(
                            column(
                                    "t",
                                    base.tenderTitle()
                            )
                    )
                    .append(
                            ") asc"
                    );
        }


        return jdbcTemplate
                .queryForList(
                        sql.toString(),
                        args.toArray()
                )
                .stream()
                .map(
                        this::tender
                )
                .toList();
    }


    @Override
    public List<MarketCpvObservation> cpvs(
            Collection<UUID> tenderIds
    ) {

        var base =
                schemaInspector
                        .schema()
                        .base();


        if (
                !base.cpvAvailable()
                ||
                tenderIds == null
                ||
                tenderIds.isEmpty()
        ) {

            return List.of();
        }


        String sql =
                "select "
                        +
                        q(
                                base.cpvTenderId()
                        )
                        +
                        " as tender_id, "
                        +
                        q(
                                base.cpvCode()
                        )
                        +
                        " as cpv_code, "
                        +
                        plainProjection(
                                base.cpvDescription(),
                                "cpv_description"
                        )
                        +
                        " from "
                        +
                        q(
                                base.cpvTable()
                        )
                        +
                        " where "
                        +
                        q(
                                base.cpvTenderId()
                        )
                        +
                        " in ("
                        +
                        placeholders(
                                tenderIds.size()
                        )
                        +
                        ")";


        return jdbcTemplate
                .queryForList(
                        sql,
                        tenderIds.toArray()
                )
                .stream()
                .map(
                        row ->
                                new MarketCpvObservation(
                                        uuid(
                                                row.get(
                                                        "tender_id"
                                                )
                                        ),

                                        string(
                                                row.get(
                                                        "cpv_code"
                                                )
                                        ),

                                        string(
                                                row.get(
                                                        "cpv_description"
                                                )
                                        )
                                )
                )
                .toList();
    }


    @Override
    public List<MarketLocationObservation> locations(
            Collection<UUID> tenderIds
    ) {

        if (
                tenderIds == null
                ||
                tenderIds.isEmpty()
        ) {

            return List.of();
        }


        var schema =
                schemaInspector
                        .schema();


        var base =
                schema.base();


        if (
                schema.locationRowsAvailable()
        ) {

            String sql =
                    "select "
                            +
                            q(
                                    schema.locationTenderId()
                            )
                            +
                            " as tender_id, "
                            +
                            plainProjection(
                                    schema.locationCountry(),
                                    "country_code"
                            )
                            +
                            ", "
                            +
                            plainProjection(
                                    schema.locationRegionCode(),
                                    "region_code"
                            )
                            +
                            ", "
                            +
                            plainProjection(
                                    schema.locationRegionName(),
                                    "region_name"
                            )
                            +
                            ", "
                            +
                            plainProjection(
                                    schema.locationCity(),
                                    "city"
                            )
                            +
                            " from "
                            +
                            q(
                                    schema.locationTable()
                            )
                            +
                            " where "
                            +
                            q(
                                    schema.locationTenderId()
                            )
                            +
                            " in ("
                            +
                            placeholders(
                                    tenderIds.size()
                            )
                            +
                            ")";


            return jdbcTemplate
                    .queryForList(
                            sql,
                            tenderIds.toArray()
                    )
                    .stream()
                    .map(
                            this::location
                    )
                    .toList();
        }


        if (
                schema.tenderCountry() == null
                &&
                schema.tenderRegionCode() == null
                &&
                schema.tenderRegionName() == null
                &&
                schema.tenderCity() == null
        ) {

            return List.of();
        }


        String sql =
                "select "
                        +
                        q(
                                base.tenderId()
                        )
                        +
                        " as tender_id, "
                        +
                        plainProjection(
                                schema.tenderCountry(),
                                "country_code"
                        )
                        +
                        ", "
                        +
                        plainProjection(
                                schema.tenderRegionCode(),
                                "region_code"
                        )
                        +
                        ", "
                        +
                        plainProjection(
                                schema.tenderRegionName(),
                                "region_name"
                        )
                        +
                        ", "
                        +
                        plainProjection(
                                schema.tenderCity(),
                                "city"
                        )
                        +
                        " from "
                        +
                        q(
                                base.tenderTable()
                        )
                        +
                        " where "
                        +
                        q(
                                base.tenderId()
                        )
                        +
                        " in ("
                        +
                        placeholders(
                                tenderIds.size()
                        )
                        +
                        ")";


        return jdbcTemplate
                .queryForList(
                        sql,
                        tenderIds.toArray()
                )
                .stream()
                .map(
                        this::location
                )
                .toList();
    }


    @Override
    public List<MarketAwardObservation> awards(
            Collection<UUID> tenderIds
    ) {

        var base =
                schemaInspector
                        .schema()
                        .base();


        if (
                !base.awardsAvailable()
                ||
                tenderIds == null
                ||
                tenderIds.isEmpty()
        ) {

            return List.of();
        }


        String sql =
                "select "
                        +
                        q(
                                base.awardId()
                        )
                        +
                        " as award_id, "
                        +
                        q(
                                base.awardTenderId()
                        )
                        +
                        " as tender_id, "
                        +
                        plainProjection(
                                base.awardAmount(),
                                "awarded_amount"
                        )
                        +
                        ", "
                        +
                        plainProjection(
                                base.awardCurrency(),
                                "award_currency"
                        )
                        +
                        " from "
                        +
                        q(
                                base.awardTable()
                        )
                        +
                        " where "
                        +
                        q(
                                base.awardTenderId()
                        )
                        +
                        " in ("
                        +
                        placeholders(
                                tenderIds.size()
                        )
                        +
                        ")";


        return jdbcTemplate
                .queryForList(
                        sql,
                        tenderIds.toArray()
                )
                .stream()
                .map(
                        row ->
                                new MarketAwardObservation(
                                        uuid(
                                                row.get(
                                                        "award_id"
                                                )
                                        ),

                                        uuid(
                                                row.get(
                                                        "tender_id"
                                                )
                                        ),

                                        decimal(
                                                row.get(
                                                        "awarded_amount"
                                                )
                                        ),

                                        string(
                                                row.get(
                                                        "award_currency"
                                                )
                                        )
                                )
                )
                .toList();
    }


    @Override
    public List<MarketSupplierObservation> suppliers(
            Collection<UUID> awardIds
    ) {

        var base =
                schemaInspector
                        .schema()
                        .base();


        if (
                !base.suppliersAvailable()
                ||
                awardIds == null
                ||
                awardIds.isEmpty()
        ) {

            return List.of();
        }


        String sql =
                "select "
                        +
                        q(
                                base.supplierAwardId()
                        )
                        +
                        " as award_id, "
                        +
                        q(
                                base.supplierName()
                        )
                        +
                        " as supplier_name"
                        +
                        " from "
                        +
                        q(
                                base.supplierTable()
                        )
                        +
                        " where "
                        +
                        q(
                                base.supplierAwardId()
                        )
                        +
                        " in ("
                        +
                        placeholders(
                                awardIds.size()
                        )
                        +
                        ")";


        return jdbcTemplate
                .queryForList(
                        sql,
                        awardIds.toArray()
                )
                .stream()
                .map(
                        row ->
                                new MarketSupplierObservation(
                                        uuid(
                                                row.get(
                                                        "award_id"
                                                )
                                        ),

                                        string(
                                                row.get(
                                                        "supplier_name"
                                                )
                                        )
                                )
                )
                .toList();
    }


    private void validateQueryCapabilities(
            MarketQuery query,
            MarketAnalyticsSchemaInspector.Schema schema
    ) {

        var base =
                schema.base();


        if (
                query.country() != null
                &&
                !schema.countryAvailable()
        ) {

            throw new InvalidRequestException(
                    "Country filtering is unavailable for the current procurement corpus"
            );
        }


        if (
                query.region() != null
                &&
                !schema.regionAvailable()
        ) {

            throw new InvalidRequestException(
                    "Region filtering is unavailable for the current procurement corpus"
            );
        }


        if (
                query.cpvPrefix() != null
                &&
                !base.cpvAvailable()
        ) {

            throw new InvalidRequestException(
                    "CPV filtering is unavailable for the current procurement corpus"
            );
        }


        if (
                query.status() != null
                &&
                base.tenderStatus() == null
        ) {

            throw new InvalidRequestException(
                    "Status filtering is unavailable for the current procurement corpus"
            );
        }


        if (
                (
                        query.dateFrom() != null
                        ||
                        query.dateTo() != null
                )
                &&
                base.tenderPublicationDate() == null
        ) {

            throw new InvalidRequestException(
                    "Publication-date filtering is unavailable for the current procurement corpus"
            );
        }
    }


    private void appendCountryFilter(
            StringBuilder sql,
            List<Object> args,
            String country,
            MarketAnalyticsSchemaInspector.Schema schema
    ) {

        if (
                country == null
        ) {

            return;
        }


        var base =
                schema.base();


        if (
                schema.locationRowsAvailable()
                &&
                schema.locationCountry() != null
        ) {

            sql.append(
                    " and exists (select 1 from "
            )
                    .append(
                            q(
                                    schema.locationTable()
                            )
                    )
                    .append(
                            " ml where "
                    )
                    .append(
                            column(
                                    "ml",
                                    schema.locationTenderId()
                            )
                    )
                    .append(
                            " = "
                    )
                    .append(
                            column(
                                    "t",
                                    base.tenderId()
                            )
                    )
                    .append(
                            " and upper("
                    )
                    .append(
                            column(
                                    "ml",
                                    schema.locationCountry()
                            )
                    )
                    .append(
                            ") = upper(?))"
                    );


            args.add(
                    country
            );

            return;
        }


        sql.append(
                " and upper("
        )
                .append(
                        column(
                                "t",
                                schema.tenderCountry()
                        )
                )
                .append(
                        ") = upper(?)"
                );


        args.add(
                country
        );
    }


    private void appendRegionFilter(
            StringBuilder sql,
            List<Object> args,
            String region,
            MarketAnalyticsSchemaInspector.Schema schema
    ) {

        if (
                region == null
        ) {

            return;
        }


        var base =
                schema.base();


        if (
                schema.locationRowsAvailable()
                &&
                (
                        schema.locationRegionCode() != null
                        ||
                        schema.locationRegionName() != null
                )
        ) {

            sql.append(
                    " and exists (select 1 from "
            )
                    .append(
                            q(
                                    schema.locationTable()
                            )
                    )
                    .append(
                            " mr where "
                    )
                    .append(
                            column(
                                    "mr",
                                    schema.locationTenderId()
                            )
                    )
                    .append(
                            " = "
                    )
                    .append(
                            column(
                                    "t",
                                    base.tenderId()
                            )
                    )
                    .append(
                            " and ("
                    );


            boolean first =
                    true;


            if (
                    schema.locationRegionCode() != null
            ) {

                sql.append(
                        "upper("
                )
                        .append(
                                column(
                                        "mr",
                                        schema.locationRegionCode()
                                )
                        )
                        .append(
                                ") = upper(?)"
                        );


                args.add(
                        region
                );

                first =
                        false;
            }


            if (
                    schema.locationRegionName() != null
            ) {

                if (
                        !first
                ) {

                    sql.append(
                            " or "
                    );
                }


                sql.append(
                        "lower("
                )
                        .append(
                                column(
                                        "mr",
                                        schema.locationRegionName()
                                )
                        )
                        .append(
                                ") = lower(?)"
                        );


                args.add(
                        region
                );
            }


            sql.append(
                    "))"
            );


            return;
        }


        sql.append(
                " and ("
        );


        boolean first =
                true;


        if (
                schema.tenderRegionCode() != null
        ) {

            sql.append(
                    "upper("
            )
                    .append(
                            column(
                                    "t",
                                    schema.tenderRegionCode()
                            )
                    )
                    .append(
                            ") = upper(?)"
                    );


            args.add(
                    region
            );


            first =
                    false;
        }


        if (
                schema.tenderRegionName() != null
        ) {

            if (
                    !first
            ) {

                sql.append(
                        " or "
                );
            }


            sql.append(
                    "lower("
            )
                    .append(
                            column(
                                    "t",
                                    schema.tenderRegionName()
                            )
                    )
                    .append(
                            ") = lower(?)"
                    );


            args.add(
                    region
            );
        }


        sql.append(
                ")"
        );
    }


    private void appendCpvFilter(
            StringBuilder sql,
            List<Object> args,
            String cpvPrefix,
            MarketAnalyticsSchemaInspector.Schema schema
    ) {

        if (
                cpvPrefix == null
        ) {

            return;
        }


        var base =
                schema.base();


        sql.append(
                " and exists (select 1 from "
        )
                .append(
                        q(
                                base.cpvTable()
                        )
                )
                .append(
                        " mc where "
                )
                .append(
                        column(
                                "mc",
                                base.cpvTenderId()
                        )
                )
                .append(
                        " = "
                )
                .append(
                        column(
                                "t",
                                base.tenderId()
                        )
                )
                .append(
                        " and "
                )
                .append(
                        column(
                                "mc",
                                base.cpvCode()
                        )
                )
                .append(
                        " like ?)"
                );


        args.add(
                cpvPrefix
                        +
                        "%"
        );
    }


    private MarketTenderObservation tender(
            Map<String, Object> row
    ) {

        return new MarketTenderObservation(
                uuid(
                        row.get(
                                "tender_id"
                        )
                ),

                uuid(
                        row.get(
                                "buyer_id"
                        )
                ),

                string(
                        row.get(
                                "buyer_name"
                        )
                ),

                string(
                        row.get(
                                "buyer_category"
                        )
                ),

                string(
                        row.get(
                                "buyer_country"
                        )
                ),

                string(
                        row.get(
                                "tender_title"
                        )
                ),

                string(
                        row.get(
                                "tender_status"
                        )
                ),

                string(
                        row.get(
                                "tender_procedure"
                        )
                ),

                string(
                        row.get(
                                "contract_nature"
                        )
                ),

                date(
                        row.get(
                                "publication_date"
                        )
                ),

                instant(
                        row.get(
                                "submission_deadline"
                        )
                ),

                decimal(
                        row.get(
                                "estimated_min_value"
                        )
                ),

                decimal(
                        row.get(
                                "estimated_max_value"
                        )
                ),

                string(
                        row.get(
                                "tender_currency"
                        )
                )
        );
    }


    private MarketLocationObservation location(
            Map<String, Object> row
    ) {

        return new MarketLocationObservation(
                uuid(
                        row.get(
                                "tender_id"
                        )
                ),

                string(
                        row.get(
                                "country_code"
                        )
                ),

                string(
                        row.get(
                                "region_code"
                        )
                ),

                string(
                        row.get(
                                "region_name"
                        )
                ),

                string(
                        row.get(
                                "city"
                        )
                )
        );
    }


    private String projection(
            String tableAlias,
            String identifier,
            String alias
    ) {

        return identifier == null
                ? "null as "
                        +
                        alias
                : column(
                        tableAlias,
                        identifier
                )
                        +
                        " as "
                        +
                        alias;
    }


    private String plainProjection(
            String identifier,
            String alias
    ) {

        return identifier == null
                ? "null as "
                        +
                        alias
                : q(
                        identifier
                )
                        +
                        " as "
                        +
                        alias;
    }


    private String column(
            String tableAlias,
            String identifier
    ) {

        if (
                tableAlias == null
                ||
                !tableAlias.matches(
                        "[A-Za-z0-9_]+"
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid table alias"
            );
        }


        return tableAlias
                +
                "."
                +
                q(
                        identifier
                );
    }


    private String q(
            String identifier
    ) {

        if (
                identifier == null
                ||
                !identifier.matches(
                        "[A-Za-z0-9_]+"
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid database identifier"
            );
        }


        return "\""
                +
                identifier
                +
                "\"";
    }


    private String placeholders(
            int count
    ) {

        return String.join(
                ",",
                Collections.nCopies(
                        count,
                        "?"
                )
        );
    }


    private UUID uuid(
            Object value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        if (
                value instanceof UUID uuid
        ) {

            return uuid;
        }


        return UUID.fromString(
                value.toString()
        );
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
                        .trim();


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

            return BigDecimal.valueOf(
                    number.doubleValue()
            );
        }


        return new BigDecimal(
                value.toString()
        );
    }


    private LocalDate date(
            Object value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        if (
                value instanceof LocalDate localDate
        ) {

            return localDate;
        }


        if (
                value instanceof Date date
        ) {

            return date.toLocalDate();
        }


        if (
                value instanceof Timestamp timestamp
        ) {

            return timestamp
                    .toLocalDateTime()
                    .toLocalDate();
        }


        if (
                value instanceof LocalDateTime dateTime
        ) {

            return dateTime.toLocalDate();
        }


        if (
                value instanceof OffsetDateTime dateTime
        ) {

            return dateTime.toLocalDate();
        }


        if (
                value instanceof Instant instant
        ) {

            return instant
                    .atOffset(
                            ZoneOffset.UTC
                    )
                    .toLocalDate();
        }


        String raw =
                value.toString();


        if (
                raw.length() >= 10
        ) {

            return LocalDate.parse(
                    raw.substring(
                            0,
                            10
                    )
            );
        }


        return null;
    }


    private Instant instant(
            Object value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        if (
                value instanceof Instant instant
        ) {

            return instant;
        }


        if (
                value instanceof Timestamp timestamp
        ) {

            return timestamp.toInstant();
        }


        if (
                value instanceof OffsetDateTime dateTime
        ) {

            return dateTime.toInstant();
        }


        if (
                value instanceof LocalDateTime dateTime
        ) {

            return dateTime
                    .toInstant(
                            ZoneOffset.UTC
                    );
        }


        if (
                value instanceof LocalDate localDate
        ) {

            return localDate
                    .atStartOfDay()
                    .toInstant(
                            ZoneOffset.UTC
                    );
        }


        return Instant.parse(
                value.toString()
        );
    }

}
