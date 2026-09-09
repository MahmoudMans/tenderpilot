package com.tenderpilot.datasource.ted;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_ted_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles(
        "test"
)
class TedIngestionMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesReusableIngestionRunTable() {

        Integer count =

                jdbcTemplate
                        .queryForObject(

                                """
                                select count(*)
                                from information_schema.tables
                                where lower(table_name) = 'data_source_ingestion_runs'
                                """,

                                Integer.class

                        );


        assertThat(
                count
        )
                .isEqualTo(
                        1
                );

    }

}
