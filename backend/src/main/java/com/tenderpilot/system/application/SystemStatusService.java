package com.tenderpilot.system.application;

import com.tenderpilot.config.AppProperties;
import com.tenderpilot.config.IntegrationProperties;
import com.tenderpilot.system.domain.DependencyStatus;
import com.tenderpilot.system.domain.SystemStatus;
import com.tenderpilot.system.infrastructure.FoundationRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Duration;
import java.time.Instant;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SystemStatusService {

    private final AppProperties appProperties;

    private final IntegrationProperties
            integrationProperties;

    private final FoundationRepository
            foundationRepository;

    private final HttpClient httpClient;

    private final Environment environment;


    public SystemStatusService(

            AppProperties appProperties,

            IntegrationProperties
                    integrationProperties,

            FoundationRepository
                    foundationRepository,

            HttpClient httpClient,

            Environment environment

    ) {

        this.appProperties =
                appProperties;

        this.integrationProperties =
                integrationProperties;

        this.foundationRepository =
                foundationRepository;

        this.httpClient =
                httpClient;

        this.environment =
                environment;

    }


    public SystemStatus currentStatus() {

        List<DependencyStatus> dependencies =
                List.of(

                        databaseStatus(),

                        httpStatus(

                                "OpenSearch",

                                integrationProperties
                                        .opensearch()
                                        .baseUrl(),

                                ""

                        ),

                        httpStatus(

                                "Ollama",

                                integrationProperties
                                        .ollama()
                                        .baseUrl(),

                                "/api/tags"

                        )

                );


        return new SystemStatus(

                appProperties.displayName(),

                appProperties.version(),

                activeEnvironment(),

                Instant.now(),

                dependencies

        );

    }


    private DependencyStatus databaseStatus() {

        try {

            String marker =

                    foundationRepository
                            .schemaMarker()
                            .orElse(
                                    "missing"
                            );


            if (
                    "missing".equals(
                            marker
                    )
            ) {

                return new DependencyStatus(

                        "PostgreSQL",

                        "DOWN",

                        "Database reachable but schema marker is missing"

                );

            }


            return new DependencyStatus(

                    "PostgreSQL",

                    "UP",

                    "Schema version " + marker

            );


        } catch (
                RuntimeException exception
        ) {

            return new DependencyStatus(

                    "PostgreSQL",

                    "DOWN",

                    "Database check failed: "
                            +
                            exception
                                    .getClass()
                                    .getSimpleName()

            );

        }

    }


    private DependencyStatus httpStatus(

            String name,

            URI baseUrl,

            String path

    ) {

        URI target =

                path.isBlank()

                        ? baseUrl

                        : baseUrl.resolve(
                                path
                        );


        HttpRequest request =

                HttpRequest
                        .newBuilder(
                                target
                        )

                        .GET()

                        .timeout(
                                Duration.ofSeconds(2)
                        )

                        .build();


        try {

            HttpResponse<Void> response =

                    httpClient.send(

                            request,

                            HttpResponse
                                    .BodyHandlers
                                    .discarding()

                    );


            if (
                    response.statusCode() >= 200
                    &&
                    response.statusCode() < 300
            ) {

                return new DependencyStatus(

                        name,

                        "UP",

                        "Reachable at "
                                +
                                baseUrl

                );

            }


            return new DependencyStatus(

                    name,

                    "DOWN",

                    "HTTP "
                            +
                            response.statusCode()
                            +
                            " from "
                            +
                            baseUrl

            );


        } catch (
                InterruptedException exception
        ) {

            Thread
                    .currentThread()
                    .interrupt();


            return new DependencyStatus(

                    name,

                    "DOWN",

                    "Health check interrupted"

            );


        } catch (
                Exception exception
        ) {

            return new DependencyStatus(

                    name,

                    "DOWN",

                    "Not reachable at "
                            +
                            baseUrl

            );

        }

    }


    private String activeEnvironment() {

        String[] profiles =
                environment
                        .getActiveProfiles();


        return profiles.length == 0

                ? "default"

                : String.join(
                        ",",
                        Arrays.asList(
                                profiles
                        )
                );

    }

}
