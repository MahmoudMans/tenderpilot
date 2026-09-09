package com.tenderpilot.system.infrastructure;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FoundationRepository {

    private final JdbcTemplate jdbcTemplate;


    public FoundationRepository(
            JdbcTemplate jdbcTemplate
    ) {

        this.jdbcTemplate =
                jdbcTemplate;

    }


    public Optional<String> schemaMarker() {

        return jdbcTemplate.query(

                """
                select metadata_value
                from platform_metadata
                where metadata_key = 'schema.version'
                """,

                resultSet ->

                        resultSet.next()

                                ? Optional.ofNullable(
                                        resultSet.getString(1)
                                )

                                : Optional.empty()

        );

    }

}
