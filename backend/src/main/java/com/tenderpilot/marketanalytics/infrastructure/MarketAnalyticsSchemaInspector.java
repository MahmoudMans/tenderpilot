package com.tenderpilot.marketanalytics.infrastructure;

import com.tenderpilot.buyerintelligence.infrastructure.ProcurementAnalyticsSchemaInspector;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class MarketAnalyticsSchemaInspector {

    private final JdbcTemplate
            jdbcTemplate;


    private final ProcurementAnalyticsSchemaInspector
            procurementSchemaInspector;


    private volatile Schema
            cached;


    public MarketAnalyticsSchemaInspector(
            JdbcTemplate jdbcTemplate,
            ProcurementAnalyticsSchemaInspector procurementSchemaInspector
    ) {

        this.jdbcTemplate =
                jdbcTemplate;

        this.procurementSchemaInspector =
                procurementSchemaInspector;
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

        ProcurementAnalyticsSchemaInspector.Schema base =
                procurementSchemaInspector
                        .schema();


        String locationTable =
                optionalTable(
                        List.of(
                                "procurement_tender_locations",
                                "tender_locations"
                        )
                );


        return new Schema(
                base,

                semanticColumn(
                        base.tenderTable(),

                        List.of(
                                "procedure",
                                "procurement_procedure",
                                "procedure_type",
                                "procedure_code"
                        ),

                        List.of(
                                "procedure"
                        )
                ),

                semanticColumn(
                        base.tenderTable(),

                        List.of(
                                "contract_nature",
                                "nature",
                                "contract_type",
                                "contract_nature_type"
                        ),

                        List.of(
                                "contract",
                                "nature"
                        )
                ),

                optionalColumn(
                        base.tenderTable(),
                        List.of(
                                "country_code",
                                "country"
                        )
                ),

                optionalColumn(
                        base.tenderTable(),
                        List.of(
                                "region_code"
                        )
                ),

                optionalColumn(
                        base.tenderTable(),
                        List.of(
                                "region_name",
                                "region"
                        )
                ),

                optionalColumn(
                        base.tenderTable(),
                        List.of(
                                "city",
                                "locality"
                        )
                ),

                locationTable,

                optionalColumn(
                        locationTable,
                        List.of(
                                "tender_id"
                        )
                ),

                optionalColumn(
                        locationTable,
                        List.of(
                                "country_code",
                                "country"
                        )
                ),

                optionalColumn(
                        locationTable,
                        List.of(
                                "region_code"
                        )
                ),

                optionalColumn(
                        locationTable,
                        List.of(
                                "region_name",
                                "region"
                        )
                ),

                optionalColumn(
                        locationTable,
                        List.of(
                                "city",
                                "locality"
                        )
                )
        );
    }


    private String semanticColumn(
            String table,
            List<String> exactCandidates,
            List<String> semanticTokens
    ) {

        String exact =
                optionalColumn(
                        table,
                        exactCandidates
                );


        if (
                exact != null
        ) {

            return exact;
        }


        if (
                table == null
                ||
                semanticTokens == null
                ||
                semanticTokens.isEmpty()
        ) {

            return null;
        }


        List<String> available =
                jdbcTemplate
                        .query(
                                """
                                select column_name
                                from information_schema.columns
                                where lower(table_name) = lower(?)
                                order by ordinal_position
                                """,

                                (
                                        rs,
                                        rowNumber
                                ) ->
                                        rs.getString(
                                                1
                                        ),

                                table
                        );


        List<String> matches =
                available
                        .stream()
                        .filter(
                                actual -> {

                                    String normalized =
                                            actual
                                                    .toLowerCase(
                                                            Locale.ROOT
                                                    );


                                    return semanticTokens
                                            .stream()
                                            .map(
                                                    token ->
                                                            token
                                                                    .toLowerCase(
                                                                            Locale.ROOT
                                                                    )
                                            )
                                            .allMatch(
                                                    normalized::contains
                                            );
                                }
                        )
                        .toList();


        if (
                matches.size() == 1
        ) {

            return matches.getFirst();
        }


        return null;
    }



    private String optionalTable(
            List<String> candidates
    ) {

        for (
                String candidate :
                candidates
        ) {

            List<String> result =
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
                    !result.isEmpty()
            ) {

                return result.getFirst();
            }
        }


        return null;
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


        Set<String> columns =
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
                    columns
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

            ProcurementAnalyticsSchemaInspector.Schema base,

            String tenderProcedure,

            String tenderContractNature,

            String tenderCountry,

            String tenderRegionCode,

            String tenderRegionName,

            String tenderCity,

            String locationTable,

            String locationTenderId,

            String locationCountry,

            String locationRegionCode,

            String locationRegionName,

            String locationCity

    ) {

        public boolean countryAvailable() {

            return (
                    locationTable != null
                    &&
                    locationTenderId != null
                    &&
                    locationCountry != null
            )
                    ||
                    tenderCountry != null;
        }


        public boolean regionAvailable() {

            return (
                    locationTable != null
                    &&
                    locationTenderId != null
                    &&
                    (
                            locationRegionCode != null
                            ||
                            locationRegionName != null
                    )
            )
                    ||
                    tenderRegionCode != null
                    ||
                    tenderRegionName != null;
        }


        public boolean locationRowsAvailable() {

            return locationTable != null
                    &&
                    locationTenderId != null;
        }

    }

}
