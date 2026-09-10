package com.tenderpilot.compliance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_compliance_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles(
        "test"
)
class BidComplianceMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesComplianceReviewTable() {

        Integer count =
                jdbcTemplate
                        .queryForObject(
                                """
                                select count(*)
                                from information_schema.tables
                                where lower(table_name) = ?
                                """,
                                Integer.class,
                                "organization_bid_compliance_reviews"
                        );


        assertThat(
                count
        )
                .isEqualTo(
                        1
                );
    }

}
