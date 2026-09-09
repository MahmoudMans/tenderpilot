package com.tenderpilot.procurement.quality;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_quality_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles("test")
class ProcurementDataQualityMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesDataQualityTables() {

        for (
                String table :
                List.of(
                        "procurement_data_quality_assessments",
                        "procurement_data_quality_issues"
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


            assertThat(count)
                    .as(
                            "Expected quality table %s",
                            table
                    )
                    .isEqualTo(
                            1
                    );
        }
    }

}
