package com.tenderpilot.bidplanning;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_bid_planning_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles(
        "test"
)
class BidPlanningMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesBidPlanningTables() {

        for (
                String table :
                List.of(
                        "organization_bid_tasks",
                        "organization_bid_submission_controls",
                        "organization_bid_planning_events"
                )
        ) {

            Integer count =
                    jdbcTemplate
                            .queryForObject(
                                    """
                                    select count(*)
                                    from information_schema.tables
                                    where lower(table_name) = ?
                                    """,
                                    Integer.class,
                                    table
                            );


            assertThat(
                    count
            )
                    .as(
                            "Expected planning table %s",
                            table
                    )
                    .isEqualTo(
                            1
                    );
        }
    }

}
