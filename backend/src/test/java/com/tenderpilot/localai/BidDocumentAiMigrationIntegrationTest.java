package com.tenderpilot.localai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_local_ai_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles(
        "test"
)
class BidDocumentAiMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesPrivateLocalAiTables() {

        for (
                String table :
                List.of(
                        "organization_bid_document_texts",
                        "organization_bid_document_ai_analyses",
                        "organization_bid_document_ai_findings"
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
                            "Expected local-AI table %s",
                            table
                    )
                    .isEqualTo(
                            1
                    );
        }
    }

}
