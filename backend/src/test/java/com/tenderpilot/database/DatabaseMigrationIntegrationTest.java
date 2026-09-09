package com.tenderpilot.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(
        "test"
)
class DatabaseMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesStepTwoSchemaBeforeApplicationTests() {

        List<String> requiredTables =

                List.of(

                        "app_users",

                        "organizations",

                        "organization_memberships",

                        "auth_refresh_tokens"

                );


        for (
                String table :
                requiredTables
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
                            "Expected Liquibase table %s",
                            table
                    )
                    .isEqualTo(
                            1
                    );

        }

    }

}
