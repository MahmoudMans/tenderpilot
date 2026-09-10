package com.tenderpilot.buyerintelligence.infrastructure;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class BuyerAnalyticsQueryGateway {

    private final JdbcTemplate
            jdbcTemplate;


    private final ProcurementAnalyticsSchemaInspector
            schemaInspector;


    public BuyerAnalyticsQueryGateway(
            JdbcTemplate jdbcTemplate,
            ProcurementAnalyticsSchemaInspector schemaInspector
    ) {

        this.jdbcTemplate =
                jdbcTemplate;

        this.schemaInspector =
                schemaInspector;
    }


    public List<BuyerRecord> buyers(
            String query,
            String country,
            int limit
    ) {

        var schema =
                schemaInspector.schema();


        StringBuilder sql =
                new StringBuilder(
                        "select "
                );


        sql.append(
                q(
                        schema.buyerId()
                )
        )
                .append(
                        " as buyer_id, "
                )
                .append(
                        q(
                                schema.buyerName()
                        )
                )
                .append(
                        " as buyer_name, "
                )
                .append(
                        projection(
                                schema.buyerCategory(),
                                "buyer_category"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                schema.buyerCountry(),
                                "buyer_country"
                        )
                )
                .append(
                        ", "
                )
                .append(
                        projection(
                                schema.buyerCity(),
                                "buyer_city"
                        )
                )
                .append(
                        " from "
                )
                .append(
                        q(
                                schema.buyerTable()
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
                            q(
                                    schema.buyerName()
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


        String cleanCountry =
                clean(
                        country
                );


        if (
                cleanCountry != null
                &&
                schema.buyerCountry()
                        !=
                        null
        ) {

            sql.append(
                    " and upper("
            )
                    .append(
                            q(
                                    schema.buyerCountry()
                            )
                    )
                    .append(
                            ") = upper(?)"
                    );


            args.add(
                    cleanCountry
            );
        }


        sql.append(
                " order by lower("
        )
                .append(
                        q(
                                schema.buyerName()
                        )
                )
                .append(
                        ") asc limit ?"
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
                        this::buyer
                )
                .toList();
    }


    public Optional<BuyerRecord> buyer(
            UUID buyerId
    ) {

        var schema =
                schemaInspector.schema();


        String sql =
                "select "
                        +
                        q(
                                schema.buyerId()
                        )
                        +
                        " as buyer_id, "
                        +
                        q(
                                schema.buyerName()
                        )
                        +
                        " as buyer_name, "
                        +
                        projection(
                                schema.buyerCategory(),
                                "buyer_category"
                        )
                        +
                        ", "
                        +
                        projection(
                                schema.buyerCountry(),
                                "buyer_country"
                        )
                        +
                        ", "
                        +
                        projection(
                                schema.buyerCity(),
                                "buyer_city"
                        )
                        +
                        " from "
                        +
                        q(
                                schema.buyerTable()
                        )
                        +
                        " where "
                        +
                        q(
                                schema.buyerId()
                        )
                        +
                        " = ?";


        return jdbcTemplate
                .queryForList(
                        sql,
                        buyerId
                )
                .stream()
                .findFirst()
                .map(
                        this::buyer
                );
    }


    public List<TenderRecord> tenders(
            UUID buyerId
    ) {

        var schema =
                schemaInspector.schema();


        String publication =
                projection(
                        schema.tenderPublicationDate(),
                        "publication_date"
                );


        String deadline =
                projection(
                        schema.tenderDeadline(),
                        "submission_deadline"
                );


        String sql =
                "select "
                        +
                        q(
                                schema.tenderId()
                        )
                        +
                        " as tender_id, "
                        +
                        q(
                                schema.tenderTitle()
                        )
                        +
                        " as tender_title, "
                        +
                        projection(
                                schema.tenderStatus(),
                                "tender_status"
                        )
                        +
                        ", "
                        +
                        publication
                        +
                        ", "
                        +
                        deadline
                        +
                        ", "
                        +
                        projection(
                                schema.tenderMinValue(),
                                "estimated_min_value"
                        )
                        +
                        ", "
                        +
                        projection(
                                schema.tenderMaxValue(),
                                "estimated_max_value"
                        )
                        +
                        ", "
                        +
                        projection(
                                schema.tenderCurrency(),
                                "tender_currency"
                        )
                        +
                        " from "
                        +
                        q(
                                schema.tenderTable()
                        )
                        +
                        " where "
                        +
                        q(
                                schema.tenderBuyerId()
                        )
                        +
                        " = ?";


        List<TenderRecord> result =
                jdbcTemplate
                        .queryForList(
                                sql,
                                buyerId
                        )
                        .stream()
                        .map(
                                this::tender
                        )
                        .toList();


        return result
                .stream()
                .sorted(
                        (
                                left,
                                right
                        ) ->
                                comparePublication(
                                        right.publicationDate(),
                                        left.publicationDate()
                                )
                )
                .toList();
    }


    public List<CpvRecord> cpvs(
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
                        projection(
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
                                new CpvRecord(
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


    public List<AwardRecord> awards(
            Collection<UUID> tenderIds
    ) {

        var schema =
                schemaInspector.schema();


        if (
                !schema.awardsAvailable()
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
                                schema.awardId()
                        )
                        +
                        " as award_id, "
                        +
                        q(
                                schema.awardTenderId()
                        )
                        +
                        " as tender_id, "
                        +
                        projection(
                                schema.awardAmount(),
                                "awarded_amount"
                        )
                        +
                        ", "
                        +
                        projection(
                                schema.awardCurrency(),
                                "award_currency"
                        )
                        +
                        " from "
                        +
                        q(
                                schema.awardTable()
                        )
                        +
                        " where "
                        +
                        q(
                                schema.awardTenderId()
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
                                new AwardRecord(
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


    public List<SupplierRecord> suppliers(
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
                        q(
                                schema.supplierAwardId()
                        )
                        +
                        " as award_id, "
                        +
                        q(
                                schema.supplierName()
                        )
                        +
                        " as supplier_name"
                        +
                        " from "
                        +
                        q(
                                schema.supplierTable()
                        )
                        +
                        " where "
                        +
                        q(
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
                                new SupplierRecord(
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


    private BuyerRecord buyer(
            Map<String, Object> row
    ) {

        return new BuyerRecord(
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
                                "buyer_city"
                        )
                )
        );
    }


    private TenderRecord tender(
            Map<String, Object> row
    ) {

        return new TenderRecord(
                uuid(
                        row.get(
                                "tender_id"
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


    private String projection(
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
                java.util.Collections.nCopies(
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
                value instanceof LocalDate date
        ) {

            return date;
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
                value instanceof LocalDateTime localDateTime
        ) {

            return localDateTime.toLocalDate();
        }


        if (
                value instanceof OffsetDateTime offsetDateTime
        ) {

            return offsetDateTime.toLocalDate();
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


        return LocalDate.parse(
                value.toString()
                        .substring(
                                0,
                                10
                        )
        );
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
                value instanceof OffsetDateTime offsetDateTime
        ) {

            return offsetDateTime.toInstant();
        }


        if (
                value instanceof LocalDateTime localDateTime
        ) {

            return localDateTime
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


    private int comparePublication(
            LocalDate left,
            LocalDate right
    ) {

        if (
                left == null
                &&
                right == null
        ) {

            return 0;
        }


        if (
                left == null
        ) {

            return -1;
        }


        if (
                right == null
        ) {

            return 1;
        }


        return left.compareTo(
                right
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


    public record BuyerRecord(

            UUID id,

            String name,

            String category,

            String countryCode,

            String city

    ) {
    }


    public record TenderRecord(

            UUID id,

            String title,

            String status,

            LocalDate publicationDate,

            Instant submissionDeadline,

            BigDecimal estimatedMinValue,

            BigDecimal estimatedMaxValue,

            String currency

    ) {
    }


    public record CpvRecord(

            UUID tenderId,

            String code,

            String description

    ) {
    }


    public record AwardRecord(

            UUID id,

            UUID tenderId,

            BigDecimal amount,

            String currency

    ) {
    }


    public record SupplierRecord(

            UUID awardId,

            String name

    ) {
    }

}
