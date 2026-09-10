package com.tenderpilot.dashboard.infrastructure;

import com.tenderpilot.buyerintelligence.infrastructure.ProcurementAnalyticsSchemaInspector;

import com.tenderpilot.dashboard.application.DashboardMatchGateway;

import com.tenderpilot.dashboard.domain.DashboardMatchObservation;

import java.sql.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

@Component
public class JdbcDashboardMatchGateway
        implements DashboardMatchGateway {

    private final JdbcTemplate
            jdbcTemplate;


    private final DashboardAnalyticsSchemaInspector
            dashboardSchemaInspector;


    private final ProcurementAnalyticsSchemaInspector
            procurementSchemaInspector;


    public JdbcDashboardMatchGateway(
            JdbcTemplate jdbcTemplate,
            DashboardAnalyticsSchemaInspector dashboardSchemaInspector,
            ProcurementAnalyticsSchemaInspector procurementSchemaInspector
    ) {

        this.jdbcTemplate =
                jdbcTemplate;

        this.dashboardSchemaInspector =
                dashboardSchemaInspector;

        this.procurementSchemaInspector =
                procurementSchemaInspector;
    }


    @Override
    public List<DashboardMatchObservation> matches(
            UUID organizationId
    ) {

        var dashboard =
                dashboardSchemaInspector
                        .schema();


        var procurement =
                procurementSchemaInspector
                        .schema();


        String sql =
                "select "
                        +
                        column(
                                "m",
                                dashboard.tenderId()
                        )
                        +
                        " as tender_id, "
                        +
                        column(
                                "m",
                                dashboard.score()
                        )
                        +
                        " as match_score, "
                        +
                        column(
                                "m",
                                dashboard.grade()
                        )
                        +
                        " as match_grade, "
                        +
                        column(
                                "m",
                                dashboard.blocked()
                        )
                        +
                        " as match_blocked, "
                        +
                        column(
                                "t",
                                procurement.tenderTitle()
                        )
                        +
                        " as tender_title, "
                        +
                        projection(
                                "t",
                                procurement.tenderDeadline(),
                                "submission_deadline"
                        )
                        +
                        ", "
                        +
                        column(
                                "b",
                                procurement.buyerId()
                        )
                        +
                        " as buyer_id, "
                        +
                        column(
                                "b",
                                procurement.buyerName()
                        )
                        +
                        " as buyer_name "
                        +
                        "from "
                        +
                        q(
                                dashboard.matchTable()
                        )
                        +
                        " m "
                        +
                        "join "
                        +
                        q(
                                procurement.tenderTable()
                        )
                        +
                        " t on "
                        +
                        column(
                                "m",
                                dashboard.tenderId()
                        )
                        +
                        " = "
                        +
                        column(
                                "t",
                                procurement.tenderId()
                        )
                        +
                        " join "
                        +
                        q(
                                procurement.buyerTable()
                        )
                        +
                        " b on "
                        +
                        column(
                                "t",
                                procurement.tenderBuyerId()
                        )
                        +
                        " = "
                        +
                        column(
                                "b",
                                procurement.buyerId()
                        )
                        +
                        " where "
                        +
                        column(
                                "m",
                                dashboard.organizationId()
                        )
                        +
                        " = ? "
                        +
                        "order by "
                        +
                        column(
                                "m",
                                dashboard.score()
                        )
                        +
                        " desc, "
                        +
                        column(
                                "t",
                                procurement.tenderTitle()
                        )
                        +
                        " asc";


        return jdbcTemplate
                .queryForList(
                        sql,
                        organizationId
                )
                .stream()
                .map(
                        this::observation
                )
                .toList();
    }


    private DashboardMatchObservation observation(
            Map<String, Object> row
    ) {

        return new DashboardMatchObservation(
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
                                "tender_title"
                        )
                ),

                string(
                        row.get(
                                "buyer_name"
                        )
                ),

                instant(
                        row.get(
                                "submission_deadline"
                        )
                ),

                integer(
                        row.get(
                                "match_score"
                        )
                ),

                string(
                        row.get(
                                "match_grade"
                        )
                ),

                bool(
                        row.get(
                                "match_blocked"
                        )
                )
        );
    }


    private String projection(
            String alias,
            String identifier,
            String projectionAlias
    ) {

        return identifier == null
                ? "null as "
                        +
                        projectionAlias
                : column(
                        alias,
                        identifier
                )
                        +
                        " as "
                        +
                        projectionAlias;
    }


    private String column(
            String alias,
            String identifier
    ) {

        if (
                alias == null
                ||
                !alias.matches(
                        "[A-Za-z0-9_]+"
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid SQL alias"
            );
        }


        return alias
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
                    "Invalid SQL identifier"
            );
        }


        return "\""
                +
                identifier
                +
                "\"";
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


    private int integer(
            Object value
    ) {

        if (
                value == null
        ) {

            return 0;
        }


        if (
                value instanceof Number number
        ) {

            return number.intValue();
        }


        return Integer.parseInt(
                value.toString()
        );
    }


    private boolean bool(
            Object value
    ) {

        if (
                value == null
        ) {

            return false;
        }


        if (
                value instanceof Boolean bool
        ) {

            return bool;
        }


        if (
                value instanceof Number number
        ) {

            return number.intValue() != 0;
        }


        return Boolean.parseBoolean(
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
