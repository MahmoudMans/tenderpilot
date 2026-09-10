package com.tenderpilot.buyerintelligence.infrastructure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class ProcurementAnalyticsSchemaInspector {

    private final JdbcTemplate
            jdbcTemplate;


    private volatile Schema
            cached;


    public ProcurementAnalyticsSchemaInspector(
            JdbcTemplate jdbcTemplate
    ) {

        this.jdbcTemplate =
                jdbcTemplate;
    }


    public Schema schema() {

        Schema current =
                cached;


        if (
                current != null
        ) {

            return current;
        }


        synchronized (
                this
        ) {

            if (
                    cached == null
            ) {

                cached =
                        inspect();
            }


            return cached;
        }
    }


    private Schema inspect() {

        String buyerTable =
                requiredTable(
                        List.of(
                                "procurement_buyers",
                                "buyers"
                        ),
                        "canonical buyer table"
                );


        String tenderTable =
                requiredTable(
                        List.of(
                                "procurement_tenders",
                                "tenders"
                        ),
                        "canonical tender table"
                );


        String cpvTable =
                optionalTable(
                        List.of(
                                "procurement_tender_cpvs",
                                "procurement_tender_cpv_codes",
                                "tender_cpvs",
                                "procurement_cpvs"
                        )
                );


        String awardTable =
                optionalTable(
                        List.of(
                                "procurement_awards",
                                "awards"
                        )
                );


        String supplierTable =
                optionalTable(
                        List.of(
                                "procurement_award_suppliers",
                                "award_suppliers",
                                "procurement_suppliers"
                        )
                );


        return new Schema(
                buyerTable,

                requiredColumn(
                        buyerTable,
                        List.of(
                                "id"
                        )
                ),

                requiredColumn(
                        buyerTable,
                        List.of(
                                "name",
                                "buyer_name",
                                "legal_name"
                        )
                ),

                optionalColumn(
                        buyerTable,
                        List.of(
                                "category",
                                "buyer_category"
                        )
                ),

                optionalColumn(
                        buyerTable,
                        List.of(
                                "country_code",
                                "country"
                        )
                ),

                optionalColumn(
                        buyerTable,
                        List.of(
                                "city",
                                "locality"
                        )
                ),

                tenderTable,

                requiredColumn(
                        tenderTable,
                        List.of(
                                "id"
                        )
                ),

                requiredColumn(
                        tenderTable,
                        List.of(
                                "buyer_id"
                        )
                ),

                requiredColumn(
                        tenderTable,
                        List.of(
                                "title"
                        )
                ),

                optionalColumn(
                        tenderTable,
                        List.of(
                                "status"
                        )
                ),

                optionalColumn(
                        tenderTable,
                        List.of(
                                "publication_date",
                                "published_at",
                                "publication_datetime"
                        )
                ),

                optionalColumn(
                        tenderTable,
                        List.of(
                                "submission_deadline",
                                "deadline"
                        )
                ),

                optionalColumn(
                        tenderTable,
                        List.of(
                                "estimated_min_value",
                                "estimated_value_min",
                                "minimum_value"
                        )
                ),

                optionalColumn(
                        tenderTable,
                        List.of(
                                "estimated_max_value",
                                "estimated_value_max",
                                "maximum_value"
                        )
                ),

                optionalColumn(
                        tenderTable,
                        List.of(
                                "currency",
                                "estimated_currency"
                        )
                ),

                cpvTable,

                optionalColumn(
                        cpvTable,
                        List.of(
                                "tender_id"
                        )
                ),

                optionalColumn(
                        cpvTable,
                        List.of(
                                "code",
                                "cpv_code"
                        )
                ),

                optionalColumn(
                        cpvTable,
                        List.of(
                                "description",
                                "cpv_description"
                        )
                ),

                awardTable,

                optionalColumn(
                        awardTable,
                        List.of(
                                "id"
                        )
                ),

                optionalColumn(
                        awardTable,
                        List.of(
                                "tender_id"
                        )
                ),

                optionalColumn(
                        awardTable,
                        List.of(
                                "awarded_amount",
                                "amount"
                        )
                ),

                optionalColumn(
                        awardTable,
                        List.of(
                                "currency",
                                "awarded_currency"
                        )
                ),

                supplierTable,

                optionalColumn(
                        supplierTable,
                        List.of(
                                "award_id"
                        )
                ),

                optionalColumn(
                        supplierTable,
                        List.of(
                                "name",
                                "supplier_name",
                                "legal_name"
                        )
                )
        );
    }


    private String requiredTable(
            List<String> candidates,
            String description
    ) {

        String result =
                optionalTable(
                        candidates
                );


        if (
                result == null
        ) {

            throw new IllegalStateException(
                    "Unable to discover "
                            +
                            description
            );
        }


        return result;
    }


    private String optionalTable(
            List<String> candidates
    ) {

        for (
                String candidate :
                candidates
        ) {

            List<String> rows =
                    jdbcTemplate
                            .query(
                                    """
                                    select table_name
                                    from information_schema.tables
                                    where lower(table_name) = lower(?)
                                    order by table_name
                                    """,

                                    (
                                            rs,
                                            rowNumber
                                    ) ->
                                            rs.getString(
                                                    1
                                            ),

                                    candidate
                            );


            if (
                    !rows.isEmpty()
            ) {

                return rows.getFirst();
            }
        }


        return null;
    }


    private String requiredColumn(
            String table,
            List<String> candidates
    ) {

        String result =
                optionalColumn(
                        table,
                        candidates
                );


        if (
                result == null
        ) {

            throw new IllegalStateException(
                    "Unable to discover required column on "
                            +
                            table
                            +
                            ": "
                            +
                            candidates
            );
        }


        return result;
    }


    private String optionalColumn(
            String table,
            List<String> candidates
    ) {

        if (
                table == null
        ) {

            return null;
        }


        Set<String> available =
                new LinkedHashSet<>(
                        jdbcTemplate
                                .query(
                                        """
                                        select column_name
                                        from information_schema.columns
                                        where lower(table_name) = lower(?)
                                        """,

                                        (
                                                rs,
                                                rowNumber
                                        ) ->
                                                rs.getString(
                                                        1
                                                ),

                                        table
                                )
                );


        for (
                String candidate :
                candidates
        ) {

            for (
                    String actual :
                    available
            ) {

                if (
                        actual
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                .equals(
                                        candidate
                                                .toLowerCase(
                                                        Locale.ROOT
                                                )
                                )
                ) {

                    return actual;
                }
            }
        }


        return null;
    }


    public record Schema(

            String buyerTable,

            String buyerId,

            String buyerName,

            String buyerCategory,

            String buyerCountry,

            String buyerCity,

            String tenderTable,

            String tenderId,

            String tenderBuyerId,

            String tenderTitle,

            String tenderStatus,

            String tenderPublicationDate,

            String tenderDeadline,

            String tenderMinValue,

            String tenderMaxValue,

            String tenderCurrency,

            String cpvTable,

            String cpvTenderId,

            String cpvCode,

            String cpvDescription,

            String awardTable,

            String awardId,

            String awardTenderId,

            String awardAmount,

            String awardCurrency,

            String supplierTable,

            String supplierAwardId,

            String supplierName

    ) {

        public boolean cpvAvailable() {

            return cpvTable != null
                    &&
                    cpvTenderId != null
                    &&
                    cpvCode != null;
        }


        public boolean awardsAvailable() {

            return awardTable != null
                    &&
                    awardId != null
                    &&
                    awardTenderId != null;
        }


        public boolean suppliersAvailable() {

            return awardsAvailable()
                    &&
                    supplierTable != null
                    &&
                    supplierAwardId != null
                    &&
                    supplierName != null;
        }

    }

}
