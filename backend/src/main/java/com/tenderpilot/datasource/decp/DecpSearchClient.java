package com.tenderpilot.datasource.decp;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DecpSearchClient
        implements DecpSearchGateway {

    private final DecpSettings settings;
    private final DecpSearchResponseParser parser;
    private final RestClient restClient;

    public DecpSearchClient(
            DecpSettings settings,
            DecpSearchResponseParser parser
    ) {

        this.settings = settings;
        this.parser = parser;

        this.restClient =
                RestClient
                        .builder()
                        .baseUrl(
                                settings.baseUrl()
                        )
                        .build();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public DecpSearchPage search(
            DecpSearchRequest request
    ) {

        try {

            Map response =
                    restClient
                            .get()
                            .uri(
                                    builder ->
                                            builder
                                                    .path(
                                                            settings.recordsPath()
                                                    )
                                                    .queryParam(
                                                            "where",
                                                            request.where()
                                                    )
                                                    .queryParam(
                                                            "order_by",
                                                            "datepublicationdonnees desc"
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
                            .body(Map.class);

            if (response == null) {
                throw new IllegalStateException(
                        "DECP Explore API returned an empty response"
                );
            }

            return parser.parse(response);

        } catch (
                RestClientException exception
        ) {

            throw new IllegalStateException(
                    "DECP Explore API request failed",
                    exception
            );
        }
    }
}
