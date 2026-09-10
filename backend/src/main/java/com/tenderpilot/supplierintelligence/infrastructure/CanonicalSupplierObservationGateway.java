package com.tenderpilot.supplierintelligence.infrastructure;

import com.tenderpilot.buyerintelligence.infrastructure.ProcurementAnalyticsSchemaInspector;

import com.tenderpilot.supplierintelligence.application.SupplierObservationGateway;

import com.tenderpilot.supplierintelligence.domain.SupplierAwardObservation;
import com.tenderpilot.supplierintelligence.domain.SupplierAwardParty;
import com.tenderpilot.supplierintelligence.domain.SupplierTenderCpv;

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
public class CanonicalSupplierObservationGateway
        implements SupplierObservationGateway {

    private final JdbcTemplate
            jdbcTemplate;


    private final ProcurementAnalyticsSchemaInspector
            schemaInspector;


    public CanonicalSupplierObservationGateway(
            JdbcTemplate jdbcTemplate,
            ProcurementAnalyticsSchemaInspector schemaInspector
    ) {

        this.jdbcTemplate =
                jdbcTemplate;

        this.schemaInspector =
                schemaInspector;
    }


    @Override
    public List<String> searchSupplierNames(
            String query,
            String buyerCountry,
            int limit
    ) {

        var schema =
                schemaInspector.schema();


        if (
                !schema.suppliersAvailable()
        ) {

            return List.of();
        }


        StringBuilder sql =
                new StringBuilder();


        sql.append(
                "select distinct "
        )
                .append(
                        column(
                                "s",
                                schema.supplierName()
                        )
                )
                .append(
                        " as supplier_name "
                )
                .append(
                        joins(
                                schema
                        )
                )
                .append(
                        " where 1 = 1"
                );


        List<Object> args =
                new ArrayList<>();


        String cleanQuery =
                clean(
                        query
                );


        if (
                cleanQuery != null
        ) {

            sql.append(
                    " and lower("
            )
                    .append(
                            column(
                                    "s",
                                    schema.supplierName()
                            )
                    )
                    .append(
                            ") like lower(?)"
                    );


            args.add(
                    "%"
                            +
                            cleanQuery
                            +
                            "%"
            );
        }


        String country =
                clean(
                        buyerCountry
                );


        if (
                country != null
                &&
                schema.buyerCountry()
                        !=
                        null
        ) {

            sql.append(
                    " and upper("
            )
                    .append(
                            column(
                                    "b",
                                    schema.buyerCountry()
                            )
                    )
                    .append(
                            ") = upper(?)"
                    );


            args.add(
                    country
            );
        }


        sql.append(
                " order by supplier_name asc limit ?"
        );


        args.add(
                Math.max(
                        1,
                        Math.min(
                                100,
                                limit
                        )
                )
        );


        return jdbcTemplate
                .queryForList(
                        sql.toString(),
                        args.toArray()
                )
                .stream()
                .map(
                        row ->
                                string(
                                        row.get(
                                                "supplier_name"
                                        )
                                )
                )
                .filter(
                        java.util.Objects::nonNull
                )
                .toList();
    }


    @Override
    public List<SupplierAwardObservation> observationsBySupplier(
            String supplierName
    ) {

        var schema =
                schemaInspector.schema();


        if (
                !schema.suppliersAvailable()
        ) {

            return List.of();
        }


        String sql =
                "select "
                        +
                        column(
                                "s",
                                schema.supplierName()
                        )
                        +
                        " as supplier_name, "
                        +
                        column(
                                "a",
                                schema.awardId()
                        )
                        +
                        " as award_id, "
                        +
                        column(
                                "t",
                                schema.tenderId()
                        )
                        +
                        " as tender_id, "
                        +
                        column(
                                "b",
                                schema.buyerId()
                        )
                        +
                        " as buyer_id, "
                        +
                        column(
                                "b",
                                schema.buyerName()
                        )
                        +
                        " as buyer_name, "
                        +
                        projection(
                                "b",
                                schema.buyerCountry(),
                                "buyer_country"
                        )
                        +
                        ", "
                        +
                        projection(
                                "b",
                                schema.buyerCity(),
                                "buyer_city"
                        )
                        +
                        ", "
                        +
                        column(
                                "t",
                                schema.tenderTitle()
                        )
                        +
                        " as tender_title, "
                        +
                        projection(
                                "t",
                                schema.tenderStatus(),
                                "tender_status"
                        )
                        +
                        ", "
                        +
                        projection(
                                "t",
                                schema.tenderPublicationDate(),
                                "publication_date"
                        )
                        +
                        ", "
                        +
                        projection(
                                "a",
                                schema.awardAmount(),
                                "awarded_amount"
                        )
                        +
                        ", "
                        +
                        projection(
                                "a",
                                schema.awardCurrency(),
                                "award_currency"
                        )
                        +
                        " "
                        +
                        joins(
                                schema
                        )
                        +
                        " where lower("
                        +
                        column(
                                "s",
                                schema.supplierName()
                        )
                        +
                        ") = lower(?)";


        return jdbcTemplate
                .queryForList(
                        sql,
                        supplierName
                )
                .stream()
                .map(
                        this::observation
                )
                .toList();
    }


    @Override
    public List<SupplierAwardParty> suppliersForAwards(
            Collection<UUID> awardIds
    ) {

        var schema =
                schemaInspector.schema();


        if (
                !schema.suppliersAvailable()
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
                        column(
                                "s",
                                schema.supplierAwardId()
                        )
                        +
                        " as award_id, "
                        +
                        column(
                                "s",
                                schema.supplierName()
                        )
                        +
                        " as supplier_name "
                        +
                        "from "
                        +
                        q(
                                schema.supplierTable()
                        )
                        +
                        " s "
                        +
                        "where "
                        +
                        column(
                                "s",
                                schema.supplierAwardId()
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
                                new SupplierAwardParty(
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
                .filter(
                        party ->
                                party.awardId() != null
                                &&
                                party.supplierName() != null
                )
                .toList();
    }


    @Override
    public List<SupplierTenderCpv> cpvsForTenders(
            Collection<UUID> tenderIds
    ) {

        var schema =
                schemaInspector.schema();


        if (
                !schema.cpvAvailable()
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
                                schema.cpvTenderId()
                        )
                        +
                        " as tender_id, "
                        +
                        q(
                                schema.cpvCode()
                        )
                        +
                        " as cpv_code, "
                        +
                        plainProjection(
                                schema.cpvDescription(),
                                "cpv_description"
                        )
                        +
                        " from "
                        +
                        q(
                                schema.cpvTable()
                        )
                        +
                        " where "
                        +
                        q(
                                schema.cpvTenderId()
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
                                new SupplierTenderCpv(
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


    private String joins(
            ProcurementAnalyticsSchemaInspector.Schema schema
    ) {

        return "from "
                +
                q(
                        schema.supplierTable()
                )
                +
                " s "
                +
                "join "
                +
                q(
                        schema.awardTable()
                )
                +
                " a on "
                +
                column(
                        "s",
                        schema.supplierAwardId()
                )
                +
                " = "
                +
                column(
                        "a",
                        schema.awardId()
                )
                +
                " join "
                +
                q(
                        schema.tenderTable()
                )
                +
                " t on "
                +
                column(
                        "a",
                        schema.awardTenderId()
                )
                +
                " = "
                +
                column(
                        "t",
                        schema.tenderId()
                )
                +
                " join "
                +
                q(
                        schema.buyerTable()
                )
                +
                " b on "
                +
                column(
                        "t",
                        schema.tenderBuyerId()
                )
                +
                " = "
                +
                column(
                        "b",
                        schema.buyerId()
                );
    }


    private SupplierAwardObservation observation(
            Map<String, Object> row
    ) {

        return new SupplierAwardObservation(
                string(
                        row.get(
                                "supplier_name"
                        )
                ),

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
                                "buyer_country"
                        )
                ),

                string(
                        row.get(
                                "buyer_city"
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

                date(
                        row.get(
                                "publication_date"
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
        );
    }


    private String projection(
            String tableAlias,
            String column,
            String alias
    ) {

        return column == null
                ? "null as "
                        +
                        alias
                : column(
                        tableAlias,
                        column
                )
                        +
                        " as "
                        +
                        alias;
    }


    private String plainProjection(
            String column,
            String alias
    ) {

        return column == null
                ? "null as "
                        +
                        alias
                : q(
                        column
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


    private String clean(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String result =
                value.trim();


        return result.isBlank()
                ? null
                : result;
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

}
