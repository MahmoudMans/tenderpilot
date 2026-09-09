package com.tenderpilot.procurement;

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
class ProcurementMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseCreatesCanonicalProcurementTables() {

        List<String> tables =

                List.of(

                        "procurement_buyers",

                        "procurement_tenders",

                        "procurement_tender_cpvs",

                        "procurement_tender_locations",

                        "procurement_tender_lots",

                        "procurement_tender_lot_cpvs",

                        "procurement_tender_sources"

                );


        for (
                String table :
                tables
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
                            "Expected canonical procurement table %s",
                            table
                    )
                    .isEqualTo(
                            1
                    );

        }

    }

}
