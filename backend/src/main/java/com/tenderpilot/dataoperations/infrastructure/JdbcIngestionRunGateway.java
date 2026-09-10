package com.tenderpilot.dataoperations.infrastructure;

import com.tenderpilot.dataoperations.application.IngestionRunGateway;

import com.tenderpilot.dataoperations.domain.IngestionRunObservation;

import java.sql.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class JdbcIngestionRunGateway
        implements IngestionRunGateway {

    private final JdbcTemplate
            jdbcTemplate;


    private final IngestionRunSchemaInspector
            schemaInspector;


    public JdbcIngestionRunGateway(
            JdbcTemplate jdbcTemplate,
            IngestionRunSchemaInspector schemaInspector
    ) {

        this.jdbcTemplate =
                jdbcTemplate;

        this.schemaInspector =
                schemaInspector;
    }


    @Override
    public List<IngestionRunObservation> recentRuns(
            int limit
    ) {

        IngestionRunSchemaInspector.Schema schema =
                schemaInspector
                        .schema();


        String sql =
                "select "
                        +
                        q(
                                schema.source()
                        )
                        +
                        " as source, "
                        +
                        projection(
                                schema.status(),
                                "run_status"
                        )
                        +
                        ", "
                        +
                        q(
                                schema.startedAt()
                        )
                        +
                        " as started_at, "
                        +
                        projection(
                                schema.finishedAt(),
                                "finished_at"
                        )
                        +
                        ", "
                        +
                        projection(
                                schema.importedCount(),
                                "imported_count"
                        )
                        +
                        ", "
                        +
                        projection(
                                schema.rejectedCount(),
                                "rejected_count"
                        )
                        +
                        " from "
                        +
                        q(
                                schema.table()
                        )
                        +
                        " order by "
                        +
                        q(
                                schema.startedAt()
                        )
                        +
                        " desc limit ?";


        return jdbcTemplate
                .queryForList(
                        sql,
                        Math.max(
                                1,
                                Math.min(
                                        5000,
                                        limit
                                )
                        )
                )
                .stream()
                .map(
                        this::observation
                )
                .toList();
    }


    private IngestionRunObservation observation(
            Map<String, Object> row
    ) {

        return new IngestionRunObservation(
                string(
                        row.get(
                                "source"
                        )
                ),

                string(
                        row.get(
                                "run_status"
                        )
                ),

                instant(
                        row.get(
                                "started_at"
                        )
                ),

                instant(
                        row.get(
                                "finished_at"
                        )
                ),

                number(
                        row.get(
                                "imported_count"
                        )
                ),

                number(
                        row.get(
                                "rejected_count"
                        )
                )
        );
    }


    private String projection(
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
                    "Invalid SQL identifier"
            );
        }


        return "\""
                +
                identifier
                +
                "\"";
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


    private Long number(
            Object value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        if (
                value instanceof Number number
        ) {

            return number.longValue();
        }


        return Long.valueOf(
                value.toString()
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
