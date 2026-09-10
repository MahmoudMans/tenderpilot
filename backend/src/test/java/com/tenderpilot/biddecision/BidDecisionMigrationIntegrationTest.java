package com.tenderpilot.biddecision;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_bid_decision_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles(
        "test"
)
class BidDecisionMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesBidDecisionTables() {

        for (
                String table :
                List.of(
                        "organization_bid_decisions",
                        "organization_bid_decision_history"
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
                            "Expected bid-decision table %s",
                            table
                    )
                    .isEqualTo(
                            1
                    );
        }
    }

}
