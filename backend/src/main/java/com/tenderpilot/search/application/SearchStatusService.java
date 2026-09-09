package com.tenderpilot.search.application;

import com.tenderpilot.search.api.SearchProjectionStatusResponse;

import com.tenderpilot.search.infrastructure.OpenSearchSettings;

import org.springframework.stereotype.Service;

@Service
public class SearchStatusService {

    private final OpenSearchGateway
            gateway;


    private final OpenSearchSettings
            settings;


    public SearchStatusService(

            OpenSearchGateway gateway,

            OpenSearchSettings settings

    ) {

        this.gateway =
                gateway;

        this.settings =
                settings;

    }


    public SearchProjectionStatusResponse status() {

        if (
                !settings.enabled()
        ) {

            return new SearchProjectionStatusResponse(
                    false,
                    false,
                    false,
                    settings.indexName(),
                    0,
                    "POSTGRESQL",
                    "POSTGRESQL"
            );

        }


        try {

            boolean reachable =
                    gateway.isAvailable();


            if (
                    !reachable
            ) {

                return new SearchProjectionStatusResponse(
                        true,
                        false,
                        false,
                        settings.indexName(),
                        0,
                        "POSTGRESQL",
                        "POSTGRESQL"
                );

            }


            boolean exists =
                    gateway.indexExists();


            long count =
                    exists
                            ? gateway.count()
                            : 0;


            return new SearchProjectionStatusResponse(
                    true,
                    true,
                    exists,
                    settings.indexName(),
                    count,
                    "POSTGRESQL",
                    "POSTGRESQL"
            );


        } catch (
                RuntimeException exception
        ) {

            return new SearchProjectionStatusResponse(
                    true,
                    false,
                    false,
                    settings.indexName(),
                    0,
                    "POSTGRESQL",
                    "POSTGRESQL"
            );

        }

    }

}
