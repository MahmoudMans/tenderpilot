package com.tenderpilot.datasource.ted;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TedSearchClient
        implements TedSearchGateway {

    private static final List<String>
            SEARCH_FIELDS =

            List.of(

                    "publication-number",
                    "notice-version",
                    "notice-type",

                    "title-proc",
                    "notice-title",
                    "description-proc",

                    "publication-date",

                    "buyer-name",
                    "buyer-identifier",
                    "buyer-country",
                    "buyer-city",
                    "buyer-internet-address",
                    "buyer-legal-type",

                    "procedure-type",

                    "contract-nature-main-proc",
                    "contract-nature",

                    "estimated-value-proc",
                    "estimated-value-cur-proc",

                    "deadline-receipt-tender-date-lot",
                    "deadline-receipt-tender-time-lot",

                    "classification-cpv",

                    "place-of-performance-country-proc",
                    "place-of-performance-subdiv-proc",
                    "place-of-performance-city-proc",

                    "framework-agreement-lot",
                    "electronic-submission-lot"

            );


    private final TedSettings settings;

    private final TedSearchResponseParser parser;

    private final RestClient restClient;


    public TedSearchClient(

            TedSettings settings,

            TedSearchResponseParser parser

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
    public TedSearchPage search(
            TedSearchRequest request
    ) {

        Map<String, Object> body =
                new LinkedHashMap<>();


        body.put(
                "query",
                request.expertQuery()
        );

        body.put(
                "fields",
                SEARCH_FIELDS
        );

        body.put(
                "page",
                request.page()
        );

        body.put(
                "limit",
                request.limit()
        );

        body.put(
                "checkQuerySyntax",
                true
        );

        body.put(
                "paginationMode",
                "PAGE_NUMBER"
        );


        try {

            Map response =

                    restClient
                            .post()
                            .uri(
                                    settings.searchPath()
                            )
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .body(
                                    body
                            )
                            .retrieve()
                            .body(
                                    Map.class
                            );


            if (
                    response == null
            ) {

                throw new IllegalStateException(
                        "TED Search API returned an empty response"
                );

            }


            return parser.parse(
                    response
            );


        } catch (
                RestClientException exception
        ) {

            throw new IllegalStateException(

                    "TED Search API request failed",

                    exception

            );

        }

    }

}
