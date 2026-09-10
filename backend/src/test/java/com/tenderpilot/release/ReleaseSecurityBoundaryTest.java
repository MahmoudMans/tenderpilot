package com.tenderpilot.release;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import org.junit.jupiter.api.Test;

class ReleaseSecurityBoundaryTest {

    @Test
    void globalSearchContainsNoPrivateWorkspaceOrDashboardData()
            throws Exception {

        String search =
                readPackage(
                        "search"
                );


        assertThat(
                search
        )
                .doesNotContain(
                        "com.tenderpilot.dashboard",
                        "com.tenderpilot.notification",
                        "organization_notifications",
                        "organization_watchlist_items",
                        "organization_bid_documents",
                        "organization_bid_rag_chunks",
                        "organization_bid_compliance_reviews",
                        "organization_bid_tasks"
                );
    }


    @Test
    void publicMarketAnalyticsRemainIndependentFromTenantPrivateDomains()
            throws Exception {

        String market =
                readPackage(
                        "marketanalytics"
                );


        assertThat(
                market
        )
                .doesNotContain(
                        "CurrentOrganizationResolver",
                        "organizationId",
                        "com.tenderpilot.company",
                        "com.tenderpilot.bidworkspace",
                        "com.tenderpilot.biddecision",
                        "com.tenderpilot.compliance",
                        "com.tenderpilot.localai",
                        "com.tenderpilot.rag",
                        "com.tenderpilot.bidplanning",
                        "com.tenderpilot.notification",
                        "com.tenderpilot.dashboard"
                );
    }


    @Test
    void dataOperationsRemainSharedAndReadOnly()
            throws Exception {

        String operations =
                readPackage(
                        "dataoperations"
                );


        assertThat(
                operations
        )
                .contains(
                        "@GetMapping"
                )
                .doesNotContain(
                        "@PostMapping",
                        "@PutMapping",
                        "@PatchMapping",
                        "@DeleteMapping",
                        "CurrentOrganizationResolver",
                        "organizationId"
                );
    }


    @Test
    void executiveDashboardRemainsReadOnlyAgainstBidDomains()
            throws Exception {

        String dashboard =
                readPackage(
                        "dashboard"
                );


        assertThat(
                dashboard
        )
                .doesNotContain(
                        ".decide(",
                        ".reopen(",
                        ".applyAssessment(",
                        ".createTask(",
                        ".updateTask(",
                        ".deleteTask(",
                        ".authorizeSubmission(",
                        ".markSubmitted("
                );
    }


    @Test
    void notificationCenterCannotMutateAuthoritativeBidState()
            throws Exception {

        String notification =
                readPackage(
                        "notification"
                );


        assertThat(
                notification
        )
                .doesNotContain(
                        ".decide(",
                        ".reopen(",
                        ".applyAssessment(",
                        ".createTask(",
                        ".updateTask(",
                        ".deleteTask(",
                        ".authorizeSubmission(",
                        ".markSubmitted("
                );


        String search =
                readPackage(
                        "search"
                );


        assertThat(
                search
        )
                .doesNotContain(
                        "com.tenderpilot.notification",
                        "organization_notifications",
                        "organization_watchlist_items"
                );
    }


    private String readPackage(
            String packageName
    )
            throws Exception {

        Path root =
                repositoryRoot()
                        .resolve(
                                "backend/src/main/java/com/tenderpilot/"
                                        +
                                        packageName
                        );


        if (
                !Files.isDirectory(
                        root
                )
        ) {

            return "";
        }


        StringBuilder result =
                new StringBuilder();


        try (
                var stream =
                        Files.walk(
                                root
                        )
        ) {

            List<Path> files =
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
                            .toList();


            for (
                    Path file :
                    files
            ) {

                result
                        .append(
                                Files.readString(
                                        file
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
