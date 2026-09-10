package com.tenderpilot.release;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import org.junit.jupiter.api.Test;

class ReleaseProductSurfaceTest {

    @Test
    void backendVersionAndRuntimeVersionAreSynchronized()
            throws Exception {

        Path root =
                repositoryRoot();


        String pom =
                Files.readString(
                        root.resolve(
                                "backend/pom.xml"
                        )
                );


        String application =
                Files.readString(
                        root.resolve(
                                "backend/src/main/resources/application.yml"
                        )
                );


        assertThat(
                pom
        )
                .contains(
                        "1.0.0"
                );


        assertThat(
                application
        )
                .contains(
                        "version: 1.0.0"
                );
    }


    @Test
    void centralNavigationContainsEveryPrimaryProductArea()
            throws Exception {

        Path root =
                repositoryRoot();


        String registry =
                Files.readString(
                        root.resolve(
                                "frontend/src/features/navigation/"
                                        +
                                        "product-navigation.ts"
                        )
                );


        assertThat(
                registry
        )
                .contains(
                        "\"/workspace\"",
                        "\"/showcase\"",
                        "\"/dashboard\"",
                        "\"/notifications\"",
                        "\"/opportunities\"",
                        "\"/matches\"",
                        "\"/bid-decisions\"",
                        "\"/market\"",
                        "\"/buyers\"",
                        "\"/suppliers\"",
                        "\"/intelligence\"",
                        "\"/data-operations\""
                );
    }


    @Test
    void everyPrimaryFrontendRouteHasAConcretePage()
            throws Exception {

        Path root =
                repositoryRoot();


        List<String> pages =
                List.of(
                        "workspace",
                        "showcase",
                        "dashboard",
                        "notifications",
                        "opportunities",
                        "matches",
                        "bid-decisions",
                        "market",
                        "buyers",
                        "suppliers",
                        "intelligence",
                        "data-operations"
                );


        for (
                String page :
                pages
        ) {

            assertThat(
                    Files.isRegularFile(
                            root.resolve(
                                    "frontend/src/app/(workspace)/"
                                            +
                                            page
                                            +
                                            "/page.tsx"
                            )
                    )
            )
                    .as(
                            "frontend route /%s",
                            page
                    )
                    .isTrue();
        }
    }


    @Test
    void primaryProtectedApiSurfacesRemainRegistered()
            throws Exception {

        Path root =
                repositoryRoot();


        String java =
                readTree(
                        root.resolve(
                                "backend/src/main/java"
                        )
                );


        assertThat(
                java
        )
                .contains(
                        "\"/api/v1/dashboard\"",
                        "\"/command-center\"",
                        "\"/api/v1/notifications\"",
                        "\"/api/v1/data-operations\"",
                        "\"/api/v1/market\"",
                        "\"/explorer\"",
                        "\"/api/v1/buyers\"",
                        "\"/api/v1/suppliers\"",
                        "\"/api/v1/matches\"",
                        "\"/api/v1/bid-decisions\"",
                        "\"/api/v1/bid-workspaces\""
                );
    }


    @Test
    void everyLiquibaseChangeFileIsRegisteredExactlyOnce()
            throws Exception {

        Path root =
                repositoryRoot();


        Path changes =
                root.resolve(
                        "backend/src/main/resources/db/changelog/changes"
                );


        String master =
                Files.readString(
                        root.resolve(
                                "backend/src/main/resources/db/changelog/"
                                        +
                                        "db.changelog-master.yaml"
                        )
                );


        List<Path> files;


        try (
                var stream =
                        Files.list(
                                changes
                        )
        ) {

            files =
                    stream
                            .filter(
                                    Files::isRegularFile
                            )
                            .filter(
                                    path ->
                                            path
                                                    .getFileName()
                                                    .toString()
                                                    .endsWith(
                                                            ".yaml"
                                                    )
                            )
                            .sorted()
                            .toList();
        }


        assertThat(
                files
        )
                .isNotEmpty();


        for (
                Path file :
                files
        ) {

            String filename =
                    file
                            .getFileName()
                            .toString();


            assertThat(
                    occurrences(
                            master,
                            filename
                    )
            )
                    .as(
                            "Liquibase registration for %s",
                            filename
                    )
                    .isEqualTo(
                            1
                    );
        }
    }


    @Test
    void reusableLocalReleaseGateIsPresent()
            throws Exception {

        Path root =
                repositoryRoot();


        assertThat(
                Files.isRegularFile(
                        root.resolve(
                                "scripts/release-gate.sh"
                        )
                )
        )
                .isTrue();
    }


    private int occurrences(
            String value,
            String token
    ) {

        int count =
                0;


        int offset =
                0;


        while (
                true
        ) {

            int index =
                    value.indexOf(
                            token,
                            offset
                    );


            if (
                    index < 0
            ) {

                return count;
            }


            count++;

            offset =
                    index
                            +
                            token.length();
        }
    }


    private String readTree(
            Path root
    )
            throws Exception {

        StringBuilder result =
                new StringBuilder();


        try (
                var stream =
                        Files.walk(
                                root
                        )
        ) {

            for (
                    Path path :
                    stream
                            .filter(
                                    Files::isRegularFile
                            )
                            .filter(
                                    path ->
                                            path
                                                    .toString()
                                                    .endsWith(
                                                            ".java"
                                                    )
                            )
                            .toList()
            ) {

                result
                        .append(
                                Files.readString(
                                        path
                                )
                        )
                        .append(
                                '\n'
                        );
            }
        }


        return result.toString();
    }


    private Path repositoryRoot() {

        Path current =
                Path.of(
                        ""
                )
                        .toAbsolutePath()
                        .normalize();


        if (
                Files.isDirectory(
                        current.resolve(
                                "backend"
                        )
                )
                &&
                Files.isDirectory(
                        current.resolve(
                                "frontend"
                        )
                )
        ) {

            return current;
        }


        Path parent =
                current.getParent();


        if (
                parent != null
                &&
                Files.isDirectory(
                        parent.resolve(
                                "backend"
                        )
                )
                &&
                Files.isDirectory(
                        parent.resolve(
                                "frontend"
                        )
                )
        ) {

            return parent;
        }


        throw new IllegalStateException(
                "Unable to locate TenderPilot repository root"
        );
    }

}
