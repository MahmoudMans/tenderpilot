package com.tenderpilot.dataoperations.infrastructure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class IngestionRunSchemaInspector {

    private final JdbcTemplate
            jdbcTemplate;


    private volatile Schema
            cached;


    public IngestionRunSchemaInspector(
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

        String table =
                requiredTable(
                        List.of(
                                "data_source_ingestion_runs"
                        )
                );


        return new Schema(
                table,

                requiredSemanticColumn(
                        table,

                        List.of(
                                "source",
                                "data_source",
                                "source_name"
                        ),

                        List.of(
                                "source"
                        )
                ),

                optionalSemanticColumn(
                        table,

                        List.of(
                                "status",
                                "run_status",
                                "ingestion_status"
                        ),

                        List.of(
                                "status"
                        )
                ),

                requiredSemanticColumn(
                        table,

                        List.of(
                                "started_at",
                                "start_time",
                                "started_on",
                                "started"
                        ),

                        List.of(
                                "start"
                        )
                ),

                optionalSemanticColumn(
                        table,

                        List.of(
                                "finished_at",
                                "completed_at",
                                "ended_at",
                                "finish_time",
                                "end_time"
                        ),

                        List.of(
                                "finish"
                        )
                ),

                optionalSemanticColumn(
                        table,

                        List.of(
                                "records_imported",
                                "imported_count",
                                "imported_records",
                                "accepted_count",
                                "success_count"
                        ),

                        List.of(
                                "import"
                        )
                ),

                optionalSemanticColumn(
                        table,

                        List.of(
                                "records_rejected",
                                "rejected_count",
                                "rejected_records",
                                "failure_count",
                                "failed_count"
                        ),

                        List.of(
                                "reject"
                        )
                )
        );
    }


    private String requiredTable(
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


        throw new IllegalStateException(
                "Unable to discover ingestion run table"
        );
    }


    private String requiredSemanticColumn(
            String table,
            List<String> exactCandidates,
            List<String> semanticTokens
    ) {

        String value =
                optionalSemanticColumn(
                        table,
                        exactCandidates,
                        semanticTokens
                );


        if (
                value == null
        ) {

            throw new IllegalStateException(
                    "Unable to uniquely discover required ingestion column on "
                            +
                            table
                            +
                            " using "
                            +
                            semanticTokens
            );
        }


        return value;
    }


    private String optionalSemanticColumn(
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


        List<String> matches =
                columns(
                        table
                )
                        .stream()
                        .filter(
                                column -> {

                                    String normalized =
                                            column
                                                    .toLowerCase(
                                                            Locale.ROOT
                                                    );


                                    return semanticTokens
                                            .stream()
                                            .map(
                                                    token ->
                                                            token.toLowerCase(
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


    private String optionalColumn(
            String table,
            List<String> candidates
    ) {

        Set<String> available =
                new LinkedHashSet<>(
                        columns(
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
                        actual.equalsIgnoreCase(
                                candidate
                        )
                ) {

                    return actual;
                }
            }
        }


        return null;
    }


    private List<String> columns(
            String table
    ) {

        return jdbcTemplate
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
    }


    public record Schema(

            String table,

            String source,

            String status,

            String startedAt,

            String finishedAt,

            String importedCount,

            String rejectedCount

    ) {
    }

}
