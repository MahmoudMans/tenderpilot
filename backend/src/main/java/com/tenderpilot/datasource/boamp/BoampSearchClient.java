package com.tenderpilot.datasource.boamp;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BoampSearchClient
        implements BoampSearchGateway {

    private final BoampSettings settings;

    private final BoampSearchResponseParser parser;

    private final RestClient restClient;


    public BoampSearchClient(

            BoampSettings settings,

            BoampSearchResponseParser parser

    ) {

        this.settings =
                settings;

        this.parser =
                parser;

        this.restClient =

                RestClient
                        .builder()
                        .baseUrl(
                                settings.baseUrl()
                        )
                        .build();

    }


    @Override
    @SuppressWarnings(
            "rawtypes"
    )
    public BoampSearchPage search(
            BoampSearchRequest request
    ) {

        try {

            Map response =

                    restClient
                            .get()
                            .uri(
                                    uriBuilder ->

                                            uriBuilder

                                                    .path(
                                                            settings.recordsPath()
                                                    )

                                                    .queryParam(
                                                            "where",
                                                            request.where()
                                                    )

                                                    .queryParam(
                                                            "order_by",
                                                            "dateparution desc"
                                                    )

                                                    .queryParam(
                                                            "limit",
                                                            request.limit()
                                                    )

                                                    .queryParam(
                                                            "offset",
                                                            request.offset()
                                                    )

                                                    .queryParam(
                                                            "lang",
                                                            "fr"
                                                    )

                                                    .queryParam(
                                                            "timezone",
                                                            "Europe/Paris"
                                                    )

                                                    .build()
                            )
                            .retrieve()
                            .body(
                                    Map.class
                            );


            if (
                    response == null
            ) {

                throw new IllegalStateException(
                        "BOAMP API returned an empty response"
                );

            }


            return parser.parse(
                    response
            );


        } catch (
                RestClientException exception
        ) {

            throw new IllegalStateException(

                    "BOAMP Explore API request failed",

                    exception

            );

        }

    }

}
