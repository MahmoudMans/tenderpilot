package com.tenderpilot.dashboard.infrastructure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class DashboardAnalyticsSchemaInspector {

    private final JdbcTemplate
            jdbcTemplate;


    private volatile Schema
            cached;


    public DashboardAnalyticsSchemaInspector(
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
                                "organization_tender_matches"
                        )
                );


        return new Schema(
                table,

                requiredColumn(
                        table,
                        List.of(
                                "organization_id"
                        )
                ),

                requiredColumn(
                        table,
                        List.of(
                                "tender_id"
                        )
                ),

                requiredSemanticColumn(
                        table,

                        List.of(
                                "score",
                                "total_score",
                                "match_score"
                        ),

                        List.of(
                                "score"
                        )
                ),

                requiredSemanticColumn(
                        table,

                        List.of(
                                "grade",
                                "match_grade"
                        ),

                        List.of(
                                "grade"
                        )
                ),

                requiredSemanticColumn(
                        table,

                        List.of(
                                "blocked",
                                "is_blocked"
                        ),

                        List.of(
                                "block"
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
                "Unable to discover organization tender match table"
        );
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
                    "Unable to discover required dashboard column on "
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


    private String requiredSemanticColumn(
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


        List<String> available =
                columns(
                        table
                );


        List<String> matches =
                available
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


        throw new IllegalStateException(
                "Unable to uniquely discover dashboard column on "
                        +
                        table
                        +
                        " for semantic tokens "
                        +
                        semanticTokens
                        +
                        ". Matches="
                        +
                        matches
        );
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
                        actual
                                .equalsIgnoreCase(
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

            String matchTable,

            String organizationId,

            String tenderId,

            String score,

            String grade,

            String blocked

    ) {
    }

}
